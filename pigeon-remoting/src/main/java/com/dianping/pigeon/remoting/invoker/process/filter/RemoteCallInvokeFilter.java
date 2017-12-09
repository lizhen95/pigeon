/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process.filter;

import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.MonitorLoader;
import com.dianping.pigeon.monitor.MonitorTransaction;
import com.dianping.pigeon.remoting.common.monitor.trace.InvokerMonitorData;
import com.dianping.pigeon.remoting.common.domain.CallMethod;
import com.dianping.pigeon.remoting.common.domain.InvocationContext.TimePhase;
import com.dianping.pigeon.remoting.common.domain.InvocationContext.TimePoint;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.exception.BadRequestException;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.concurrent.CallbackFuture;
import com.dianping.pigeon.remoting.invoker.concurrent.FutureFactory;
import com.dianping.pigeon.remoting.invoker.concurrent.InvocationCallback;
import com.dianping.pigeon.remoting.invoker.concurrent.ServiceCallbackWrapper;
import com.dianping.pigeon.remoting.invoker.concurrent.ServiceFutureImpl;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.DefaultInvokerContext;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.remoting.invoker.route.region.Region;
import com.dianping.pigeon.remoting.invoker.util.InvokerHelper;
import com.dianping.pigeon.remoting.invoker.util.InvokerUtils;

/**
 * 执行实际的Remote Call，包括Sync, Future，Callback，Oneway
 *
 * @author danson.liu
 */
public class RemoteCallInvokeFilter extends InvocationInvokeFilter {

    private static final Logger logger = LoggerLoader.getLogger(RemoteCallInvokeFilter.class);
    private static final InvocationResponse NO_RETURN_RESPONSE = InvokerUtils.createNoReturnResponse();

    @Override
    public InvocationResponse invoke(ServiceInvocationHandler handler, InvokerContext invocationContext)
            throws Throwable {
        invocationContext.getTimeline().add(new TimePoint(TimePhase.Q));
        Client client = invocationContext.getClient();
        InvocationRequest request = invocationContext.getRequest();
        InvokerConfig<?> invokerConfig = invocationContext.getInvokerConfig();
        byte callMethodCode = invokerConfig.getCallMethod(invocationContext.getMethodName());

        beforeInvoke(invocationContext);
        boolean isCancel = InvokerHelper.getCancel();
        if (isCancel) {
            return InvokerUtils.createDefaultResponse(InvokerHelper.getDefaultResult());
        }
        InvocationResponse response = null;
        Integer timeoutThreadLocal = InvokerHelper.getTimeout();
        if (timeoutThreadLocal != null) {
            request.setTimeout(timeoutThreadLocal.intValue());
        }

        MonitorTransaction transaction = MonitorLoader.getMonitor().getCurrentCallTransaction();
        if (transaction != null) {
            transaction.addData("CurrentTimeout", request.getTimeout());
        }

        CallMethod callMethod = CallMethod.getCallMethod(callMethodCode);

        InvokerMonitorData monitorData = (InvokerMonitorData) invocationContext.getMonitorData();

        if (monitorData != null) {
            monitorData.setCallMethod(callMethodCode);
            monitorData.setSerialize(request.getSerialize());
            monitorData.setTimeout(request.getTimeout());

            Region region = client.getRegion();

            monitorData.setRegion(region == null ? null : region.getName());
            monitorData.add();
        }

        try {
            switch (callMethod) {
                case SYNC:
                    CallbackFuture future = new CallbackFuture();
                    response = InvokerUtils.sendRequest(client, invocationContext.getRequest(), future);
                    invocationContext.getTimeline().add(new TimePoint(TimePhase.Q));
                    if (response == null) {
                        response = future.getResponse(request.getTimeout());
                    }
                    break;
                case CALLBACK:
                    InvocationCallback callback = invokerConfig.getCallback();
                    InvocationCallback tlCallback = InvokerHelper.getCallback();
                    if (tlCallback != null) {
                        callback = tlCallback;
                        InvokerHelper.clearCallback();
                    }
                    InvokerUtils.sendRequest(client, invocationContext.getRequest(), new ServiceCallbackWrapper(
                            invocationContext, callback));
                    response = NO_RETURN_RESPONSE;
                    invocationContext.getTimeline().add(new TimePoint(TimePhase.Q));
                    break;
                case FUTURE:
                    ServiceFutureImpl futureImpl = new ServiceFutureImpl(invocationContext, request.getTimeout());
                    InvokerUtils.sendRequest(client, invocationContext.getRequest(), futureImpl);
                    FutureFactory.setFuture(futureImpl);
                    response = InvokerUtils.createFutureResponse(futureImpl);
                    invocationContext.getTimeline().add(new TimePoint(TimePhase.Q));
                    break;
                case ONEWAY:
                    InvokerUtils.sendRequest(client, invocationContext.getRequest(), null);
                    response = NO_RETURN_RESPONSE;
                    invocationContext.getTimeline().add(new TimePoint(TimePhase.Q));
                    break;
                default:
                    throw new BadRequestException("Call type[" + callMethod.getName() + "] is not supported!");

            }

            ((DefaultInvokerContext) invocationContext).setResponse(response);
            afterInvoke(invocationContext);
        } catch (Throwable t) {
            afterThrowing(invocationContext, t);
            throw t;
        }

        return response;
    }

}
