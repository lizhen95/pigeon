package com.dianping.pigeon.remoting.invoker.concurrent;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLoader;
import com.dianping.pigeon.monitor.MonitorTransaction;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.domain.generic.UnifiedResponse;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.common.util.ContextUtils;
import com.dianping.pigeon.remoting.common.util.InvocationUtils;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.process.ExceptionManager;
import com.dianping.pigeon.remoting.invoker.process.InvokerContextProcessor;
import com.dianping.pigeon.remoting.invoker.route.statistics.ServiceStatisticsHolder;

/**
 * Created by chenchongze on 16/9/9.
 */
public class CallbackFuture implements Callback, CallFuture {
    private static final Logger logger = LoggerLoader.getLogger(CallbackFuture.class);
    protected static final Monitor monitor = MonitorLoader.getMonitor();
    private static final InvokerContextProcessor contextProcessor = ExtensionLoader
            .getExtension(InvokerContextProcessor.class);

    protected InvocationResponse response;
    private boolean done = false;
    private boolean cancelled = false;
    protected InvocationRequest request;
    protected Client client;
    protected MonitorTransaction transaction;

    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    public CallbackFuture() {
        transaction = monitor.getCurrentCallTransaction();
    }

    @Override
    public void callback(InvocationResponse response) {
        this.response = response;
    }

    @Override
    public void run() {
        lock.lock();
        try {
            this.done = true;
            if (condition != null) {
                condition.signal();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isDone() {
        return this.done;
    }

    protected InvocationResponse waitResponse(long timeoutMillis) throws InterruptedException {
        if (response != null && response.getMessageType() == Constants.MESSAGE_TYPE_SERVICE) {
            return response;
        }
        if (request == null && response != null) {
            return response;
        }

        lock.lock();
        try {
            long start = request.getCreateMillisTime();
            long timeoutLeft = timeoutMillis;

            while (!isDone()) {
                condition.await(timeoutLeft, TimeUnit.MILLISECONDS);
                long timeoutPassed = System.currentTimeMillis() - start;

                if (isDone() || timeoutPassed >= timeoutMillis) {
                    break;
                } else {
                    timeoutLeft = timeoutMillis - timeoutPassed;
                }
            }
        } finally {
            lock.unlock();
        }

        if (!isDone()) {
            if (client != null) {
                ServiceStatisticsHolder.flowOut(request, client.getAddress());
            }
            throw InvocationUtils.newTimeoutException(
                    "request timeout, current time:" + System.currentTimeMillis() + "\r\nrequest:" + request);
        }

        return this.response;
    }

    @Override
    public InvocationResponse getResponse() throws InterruptedException {
        return getResponse(Long.MAX_VALUE);
    }

    @Override
    public InvocationResponse getResponse(long timeout, TimeUnit unit) throws InterruptedException {
        return getResponse(unit.toMillis(timeout));
    }

    @Override
    public InvocationResponse getResponse(long timeoutMillis) throws InterruptedException {
        waitResponse(timeoutMillis);
        processContext();

        if (response.getMessageType() == Constants.MESSAGE_TYPE_EXCEPTION) {
            String addr = client != null ? client.getAddress() : "";
            ExceptionManager.INSTANCE.logRemoteCallException(addr, request.getServiceName(),
                    request.getMethodName(), "remote call error", request, response, transaction);
        } else if (response.getMessageType() == Constants.MESSAGE_TYPE_SERVICE_EXCEPTION) {
            ExceptionManager.INSTANCE.logRemoteServiceException("remote service biz error", request, response);
        }

        return this.response;
    }

    @Override
    public void setRequest(InvocationRequest request) {
        this.request = request;
    }

    @Override
    public void setClient(Client client) {
        this.client = client;
    }

    @Override
    public Client getClient() {
        return this.client;
    }

    @Override
    public void dispose() {

    }

    @Override
    public boolean cancel() {
        return this.cancelled;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    protected void processContext() {
        if (response == null) {
            return;
        }
        if (response instanceof UnifiedResponse) {
            processContext0((UnifiedResponse) response);
        } else {
            processContext0();
        }
    }

    protected void processContext0() {
        Map<String, Serializable> responseValues = response.getResponseValues();
        if (responseValues != null) {
            ContextUtils.setResponseContext(responseValues);
        }

        if (contextProcessor != null) {
            contextProcessor.postInvoke(response);
        }
    }

    protected void processContext0(UnifiedResponse response) {
        if (response != null) {
            UnifiedResponse _response = (UnifiedResponse) response;
            Map<String, String> responseValues = _response.getLocalContext();
            if (responseValues != null) {
                ContextUtils.setResponseContext((Map) responseValues);
            }
        }
    }

    protected void setResponseContext(InvocationResponse response) {
        if (response == null) {
            return;
        }

        if (response instanceof UnifiedResponse) {
            UnifiedResponse _response = (UnifiedResponse) response;
            Map<String, String> responseValues = _response.getLocalContext();
            if (responseValues != null) {
                ContextUtils.setResponseContext((Map) responseValues);
            }
        } else {
            Map<String, Serializable> responseValues = response.getResponseValues();
            if (responseValues != null) {
                ContextUtils.setResponseContext(responseValues);
            }
        }
    }

}