/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process.filter;

import com.dianping.pigeon.log.Logger;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.remoting.common.domain.CallMethod;
import com.dianping.pigeon.remoting.common.domain.CallType;
import com.dianping.pigeon.remoting.common.domain.InvocationContext.TimePhase;
import com.dianping.pigeon.remoting.common.domain.InvocationContext.TimePoint;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.concurrent.FutureFactory;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.remoting.invoker.process.statistics.InvokerStatisticsChecker;
import com.dianping.pigeon.remoting.invoker.process.statistics.InvokerStatisticsHolder;
import com.dianping.pigeon.threadpool.DefaultThreadPool;
import com.dianping.pigeon.threadpool.ThreadPool;

/**
 *
 *
 */
public class GatewayInvokeFilter extends InvocationInvokeFilter {

    private static final Logger logger = LoggerLoader.getLogger(GatewayInvokeFilter.class);
    private static ThreadPool statisticsCheckerPool = new DefaultThreadPool("Pigeon-Client-Statistics-Checker");

    static {
        InvokerStatisticsChecker appStatisticsChecker = new InvokerStatisticsChecker();
        statisticsCheckerPool.execute(appStatisticsChecker);
    }

    @Override
    public InvocationResponse invoke(ServiceInvocationHandler handler, InvokerContext invocationContext)
            throws Throwable {
        invocationContext.getTimeline().add(new TimePoint(TimePhase.G));
        InvokerConfig<?> invokerConfig = invocationContext.getInvokerConfig();
        InvocationRequest request = invocationContext.getRequest();
        Client client = invocationContext.getClient();
        String targetApp = RegistryManager.getInstance().getReferencedAppFromCache(client.getAddress());
        try {
            InvokerStatisticsHolder.flowIn(request, targetApp);
            try {
                return handler.handle(invocationContext);
            } catch (Throwable e) {
                if (CallMethod.isFuture(invokerConfig.getCallType())) {
                    FutureFactory.remove();
                }
                throw e;
            }
        } finally {
            InvokerStatisticsHolder.flowOut(request, targetApp);
        }
    }

}
