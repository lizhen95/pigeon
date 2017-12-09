/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.domain;

import java.util.concurrent.Future;

import com.dianping.pigeon.remoting.common.domain.AbstractInvocationContext;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.provider.service.method.ServiceMethod;

public class DefaultProviderContext extends AbstractInvocationContext implements ProviderContext {
    private Throwable serviceError;
    private Throwable frameworkError;
    private ProviderChannel channel;
    private Future<?> future;
    private Thread thread;
    private ServiceMethod serviceMethod;
    private String methodUri;
    private boolean async = false;

    public DefaultProviderContext(InvocationRequest request, ProviderChannel channel) {
        super(request);
        this.channel = channel;
        getTimeline().add(new TimePoint(TimePhase.R, request.getCreateMillisTime()));
        getTimeline().add(new TimePoint(TimePhase.R, System.currentTimeMillis()));
    }

    public Throwable getServiceError() {
        return serviceError;
    }

    public void setServiceError(Throwable serviceError) {
        this.serviceError = serviceError;
    }

    public Throwable getFrameworkError() {
        return frameworkError;
    }

    public void setFrameworkError(Throwable frameworkError) {
        this.frameworkError = frameworkError;
    }

    @Override
    public ProviderChannel getChannel() {
        return channel;
    }

    @Override
    public void setFuture(Future future) {
        this.future = future;
    }

    @Override
    public Future<?> getFuture() {
        return this.future;
    }

    @Override
    public Thread getThread() {
        return thread;
    }

    @Override
    public void setThread(Thread thread) {
        this.thread = thread;
    }

    @Override
    public void setServiceMethod(ServiceMethod serviceMethod) {
        this.serviceMethod = serviceMethod;
    }

    @Override
    public ServiceMethod getServiceMethod() {
        return serviceMethod;
    }

    @Override
    public boolean isAsync() {
        return async;
    }

    @Override
    public void setAsync(boolean async) {
        this.async = async;
    }

    @Override
    public String getMethodUri() {
        return methodUri;
    }

    @Override
    public void setMethodUri(String uri) {
        this.methodUri = uri;
    }

}
