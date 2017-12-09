/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process.filter;

import java.util.Calendar;

import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLoader;
import com.dianping.pigeon.monitor.MonitorTransaction;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.remoting.common.codec.SerializerType;
import com.dianping.pigeon.remoting.common.domain.CallMethod;
import com.dianping.pigeon.remoting.common.domain.InvocationContext.TimePhase;
import com.dianping.pigeon.remoting.common.domain.InvocationContext.TimePoint;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.monitor.SizeMonitor;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.common.util.InvocationUtils;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.remoting.invoker.process.DegradationManager;
import com.dianping.pigeon.remoting.invoker.process.ExceptionManager;

public class RemoteCallMonitorInvokeFilter extends InvocationInvokeFilter {

    private static final Logger logger = LoggerLoader.getLogger(RemoteCallMonitorInvokeFilter.class);

    private final Monitor monitor = MonitorLoader.getMonitor();

    public RemoteCallMonitorInvokeFilter() {
    }

    @Override
    public InvocationResponse invoke(ServiceInvocationHandler handler, InvokerContext invocationContext)
            throws Throwable {
        invocationContext.getTimeline().add(new TimePoint(TimePhase.O));
        MonitorTransaction transaction = null;
        InvocationRequest request = invocationContext.getRequest();
        String targetApp = null;
        String callInterface = null;
        InvokerConfig<?> invokerConfig = invocationContext.getInvokerConfig();
        byte callMethodCode = invokerConfig.getCallMethod(invocationContext.getMethodName());
        CallMethod callMethod = CallMethod.getCallMethod(callMethodCode);
        if (monitor != null) {
            try {
                callInterface = InvocationUtils.getRemoteCallFullName(invokerConfig.getUrl(),
                        invocationContext.getMethodName(), invocationContext.getParameterTypes());
                transaction = monitor.createTransaction("PigeonCall", callInterface, invocationContext);
                if (transaction != null) {
                    monitor.setCurrentCallTransaction(transaction);
                    transaction.setStatusOk();
                    transaction.logEvent("PigeonCall.callType",
                            invokerConfig.getCallType(invocationContext.getMethodName()), "");
                    transaction.logEvent("PigeonCall.serialize", invokerConfig.getSerialize() + "", "");
                    transaction.logEvent("PigeonCall.timeout",
                            invokerConfig.getTimeout(invocationContext.getMethodName()) + "", "");
                    transaction.logEvent("PigeonCall.QPS", "S" + Calendar.getInstance().get(Calendar.SECOND), "");
                }
            } catch (Throwable e) {
                monitor.logMonitorError(e);
            }
        }
        boolean error = false;
        try {
            InvocationResponse response = handler.handle(invocationContext);
            if (transaction != null) {
                if (CallMethod.SYNC == callMethod) {
                    DegradationManager.INSTANCE.monitorDegrade(invocationContext, transaction);
                }
                Client client = invocationContext.getClient();
                if (client != null) {
                    targetApp = RegistryManager.getInstance().getReferencedAppFromCache(client.getAddress());
                    transaction.logEvent("PigeonCall.app", targetApp, "");
                    String parameters = "";
                    if (request != null && Constants.LOG_PARAMETERS) {
                        parameters = InvocationUtils.toJsonString(request.getParameters(), 1000, 50);
                    }
                    transaction.logEvent("PigeonCall.server", client.getAddress(), parameters);
                }

                request = invocationContext.getRequest();

                if (request != null) {
                    String reqSize = SizeMonitor.getInstance().getLogSize(request.getSize());
                    if (reqSize != null) {
                        monitor.logEvent("PigeonCall.requestSize", reqSize, "" + request.getSize());
                    }
                }
                if (response != null && response.getSize() > 0) {
                    String respSize = SizeMonitor.getInstance().getLogSize(response.getSize());
                    if (respSize != null) {
                        monitor.logEvent("PigeonCall.responseSize", respSize, "" + response.getSize());
                    }
                    invocationContext.getTimeline().add(new TimePoint(TimePhase.R, response.getCreateMillisTime()));
                    invocationContext.getTimeline().add(new TimePoint(TimePhase.R));
                }
            }
            return response;
        } catch (Throwable e) {
            if (transaction != null) {
                if (CallMethod.SYNC == callMethod || CallMethod.ONEWAY == callMethod) {
                    DegradationManager.INSTANCE.monitorDegrade(invocationContext, transaction);
                }
                Client client = invocationContext.getClient();
                String remoteAddress = null;
                if (client != null) {
                    remoteAddress = client.getAddress();
                    targetApp = RegistryManager.getInstance().getReferencedAppFromCache(remoteAddress);
                    transaction.logEvent("PigeonCall.app", targetApp, "");
                    String parameters = "";
                    if (request != null && Constants.LOG_PARAMETERS) {
                        parameters = InvocationUtils.toJsonString(request.getParameters(), 1000, 50);
                    }
                    transaction.logEvent("PigeonCall.server", client.getAddress(), parameters);
                }
                request = invocationContext.getRequest();
                if (request != null) {
                    String reqSize = SizeMonitor.getInstance().getLogSize(request.getSize());
                    if (reqSize != null) {
                        monitor.logEvent("PigeonCall.requestSize", reqSize, "" + request.getSize());
                    }
                }

                ExceptionManager.INSTANCE.logRpcException(remoteAddress, invokerConfig.getUrl(),
                        invocationContext.getMethodName(), "", e, request, null, transaction);
            }
            throw e;
        } finally {

            if (transaction != null) {
                try {
                    if (invocationContext.getRequest() != null) {
                        InvocationRequest _request = invocationContext.getRequest();
                        InvokerConfig config = invocationContext.getInvokerConfig();
                        if (_request.getSerialize() != config.getSerialize()) {
                            transaction.addData("CurrentSerialize", _request.getSerialize());
                        }
                        monitorProtocol(invocationContext, _request, targetApp);
                    }
                    invocationContext.getTimeline().add(new TimePoint(TimePhase.E, System.currentTimeMillis()));
                    transaction.complete();
                } catch (Throwable e) {
                    monitor.logMonitorError(e);
                }
                if (monitor != null) {
                    monitor.clearCallTransaction();
                }
            }
        }
    }


    private void monitorProtocol(InvokerContext invokerContext, InvocationRequest request, String targetApp) {
        if (SerializerType.isThrift(request.getSerialize())) {
            Client client = invokerContext.getClient();
            String address = "NULL";
            if (client != null) {
                address = client.getAddress();
            }
            monitor.logEvent("PigeonCall.protocol", targetApp, address);
        }
    }

}
