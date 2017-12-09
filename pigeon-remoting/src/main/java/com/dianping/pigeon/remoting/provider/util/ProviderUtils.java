/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.util;

import java.util.HashMap;
import java.util.Map;

import com.dianping.pigeon.remoting.common.domain.InvocationSerializable;
import com.dianping.pigeon.remoting.common.domain.generic.UnifiedInvocation;
import org.apache.commons.lang.StringUtils;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.codec.SerializerType;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.domain.generic.UnifiedRequest;
import com.dianping.pigeon.remoting.common.domain.generic.UnifiedResponse;
import com.dianping.pigeon.remoting.common.exception.BadRequestException;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.common.util.InvocationUtils;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.process.ProviderExceptionTranslator;
import com.dianping.pigeon.remoting.provider.publish.ServicePublisher;
import com.dianping.pigeon.util.LangUtils;
import com.dianping.pigeon.util.VersionUtils;

public final class ProviderUtils {

    private static ConfigManager configManager = ConfigManagerLoader.getConfigManager();

    private static ProviderExceptionTranslator exceptionTranslator = new ProviderExceptionTranslator();

    private ProviderUtils() {
    }

    public static InvocationResponse createThrowableResponse(long seq, byte serialization, Throwable e) {
        InvocationResponse response = null;
        response = SerializerFactory.getSerializer(serialization).newResponse();
        response.setSequence(seq);
        response.setSerialize(serialization);
        response.setMessageType(Constants.MESSAGE_TYPE_EXCEPTION);
        if (SerializerType.isJson(serialization)) {
            response.setReturn(LangUtils.getFullStackTrace(e));
        } else {
            response.setReturn(exceptionTranslator.translate(e));
        }
        return response;
    }

    public static InvocationResponse createThrowableResponse(InvocationSerializable invocation, byte serialization, Throwable e) {
        if (invocation instanceof UnifiedInvocation) {
            if (invocation instanceof UnifiedRequest) {
                return createThrowableResponse0((UnifiedRequest) invocation, e);
            } else if (invocation instanceof UnifiedResponse) {
                return createThrowableResponse0((UnifiedResponse) invocation, e);
            } else {
                throw new IllegalArgumentException("unsupported this class " + invocation.getClass());
            }
        } else {
            return createThrowableResponse(invocation.getSequence(), serialization, e);
        }
    }

    public static InvocationResponse createThrowableResponse(InvocationRequest request, byte serialization, Throwable e) {
        if (request instanceof UnifiedRequest) {
            return createThrowableResponse0((UnifiedRequest) request, e);
        } else {
            return createThrowableResponse0(request, e);
        }
    }


    public static InvocationResponse createThrowableResponse0(InvocationRequest request, Throwable e) {
        InvocationResponse response = null;
        byte serialize = request.getSerialize();
        response = SerializerFactory.getSerializer(serialize).newResponse();
        response.setSequence(request.getSequence());
        response.setSerialize(serialize);
        response.setMessageType(Constants.MESSAGE_TYPE_EXCEPTION);
        if (SerializerType.isJson(serialize)) {
            response.setReturn(LangUtils.getFullStackTrace(e));
        } else {
            response.setReturn(exceptionTranslator.translate(e));
        }
        return response;
    }

    public static InvocationResponse createThrowableResponse0(UnifiedRequest request, Throwable e) {
        UnifiedResponse response = null;
        byte serialize = request.getSerialize();
        response = (UnifiedResponse) SerializerFactory.getSerializer(serialize).newResponse();
        response.setSequence(request.getSequence());
        response.setSerialize(serialize);
        response.setServiceName(request.getServiceName());
        response.setMethodName(request.getMethodName());
        response.setMessageType(Constants.MESSAGE_TYPE_EXCEPTION);
        if (SerializerType.isJson(serialize)) {
            response.setReturn(LangUtils.getFullStackTrace(e));
        } else {
            response.setReturn(exceptionTranslator.translate(e));
        }
        response.setSeqId(request.getSeqId());
        return response;
    }

    public static InvocationResponse createThrowableResponse0(UnifiedResponse response, Throwable e) {
        UnifiedResponse response0 = null;
        byte serialize = response.getSerialize();
        response0 = (UnifiedResponse) SerializerFactory.getSerializer(serialize).newResponse();
        response0.setSequence(response.getSequence());
        response0.setSerialize(serialize);
        response0.setServiceName(response.getServiceName());
        response0.setMethodName(response.getMethodName());
        response0.setMessageType(Constants.MESSAGE_TYPE_EXCEPTION);
        if (SerializerType.isJson(serialize)) {
            response0.setReturn(LangUtils.getFullStackTrace(e));
        } else {
            response0.setReturn(exceptionTranslator.translate(e));
        }
        response0.setSeqId(response.getSeqId());
        return response;
    }


    public static InvocationResponse createFailResponse(InvocationRequest request, Throwable e) {
        InvocationResponse response = null;
        if (request.getMessageType() == Constants.MESSAGE_TYPE_HEART) {
            response = InvocationUtils.newResponse(request.getSerialize(), request.getSequence(), Constants.MESSAGE_TYPE_HEART,
                    exceptionTranslator.translate(e));
        } else {
            response = createThrowableResponse(request, request.getSerialize(), e);
        }
        return response;
    }

    public static InvocationResponse createServiceExceptionResponse(InvocationRequest request, Throwable e) {
        if (request instanceof UnifiedRequest) {
            return createServiceExceptionResponse0((UnifiedRequest) request, e);
        } else {
            return createServiceExceptionResponse0(request, e);
        }
    }

    public static InvocationResponse createServiceExceptionResponse0(InvocationRequest request, Throwable e) {
        InvocationResponse response = null;
        byte serialize = request.getSerialize();
        response = SerializerFactory.getSerializer(serialize).newResponse();
        response.setSequence(request.getSequence());
        response.setSerialize(serialize);
        response.setMessageType(Constants.MESSAGE_TYPE_SERVICE_EXCEPTION);
        if (SerializerType.isJson(serialize)) {
            response.setReturn(LangUtils.getFullStackTrace(e));
        } else {
            response.setReturn(e);
        }

        return response;
    }

    public static InvocationResponse createServiceExceptionResponse0(UnifiedRequest request, Throwable e) {
        UnifiedResponse response = null;
        byte serialize = request.getSerialize();
        response = (UnifiedResponse) SerializerFactory.getSerializer(serialize).newResponse();
        response.setSequence(request.getSequence());
        response.setSerialize(serialize);
        response.setMessageType(Constants.MESSAGE_TYPE_SERVICE_EXCEPTION);
        response.setServiceName(request.getServiceName());
        response.setMethodName(request.getMethodName());
        if (SerializerType.isJson(serialize)) {
            response.setReturn(LangUtils.getFullStackTrace(e));
        } else {
            response.setReturn(e);
        }
        response.setSeqId(request.getSeqId());
        return response;
    }

    public static InvocationResponse createSuccessResponse(InvocationRequest request, Object returnObj) {
        if (request instanceof UnifiedRequest) {
            return createSuccessResponse0((UnifiedRequest) request, returnObj);
        } else {
            return createSuccessResponse0(request, returnObj);
        }
    }

    public static InvocationResponse createSuccessResponse0(InvocationRequest request, Object returnObj) {
        InvocationResponse response = null;
        byte serialize = request.getSerialize();
        response = SerializerFactory.getSerializer(serialize).newResponse();
        response.setSequence(request.getSequence());
        response.setSerialize(serialize);
        response.setMessageType(Constants.MESSAGE_TYPE_SERVICE);
        response.setReturn(returnObj);

        return response;
    }

    public static InvocationResponse createSuccessResponse0(UnifiedRequest request, Object returnObj) {
        UnifiedResponse response = null;
        byte serialize = request.getSerialize();
        response = (UnifiedResponse) SerializerFactory.getSerializer(serialize).newResponse();
        response.setSequence(request.getSequence());
        response.setSerialize(serialize);
        response.setMessageType(Constants.MESSAGE_TYPE_SERVICE);
        response.setServiceName(request.getServiceName());
        response.setMethodName(request.getMethodName());
        response.setReturn(returnObj);
        response.setSeqId(request.getSeqId());
        return response;
    }

    public static InvocationResponse createHeartResponse(InvocationRequest request) {
        if (request instanceof UnifiedRequest) {
            return createHeartResponse0((UnifiedRequest) request);
        } else {
            return createHeartResponse0(request);
        }
    }

    public static InvocationResponse createHeartResponse0(UnifiedRequest request) {
        UnifiedResponse response = null;
        byte serialize = request.getSerialize();
        response = (UnifiedResponse) SerializerFactory.getSerializer(serialize).newResponse();
        response.setSequence(request.getSequence());
        response.setSerialize(serialize);
        response.setMessageType(Constants.MESSAGE_TYPE_HEART);
        response.setServiceName(request.getServiceName());
        response.setMethodName(request.getMethodName());
        response.setSeqId(request.getSeqId());
        response.setCreateMillisTime(System.currentTimeMillis());
        return response;
    }

    public static InvocationResponse createHeartResponse0(InvocationRequest request) {
        InvocationResponse response = InvocationUtils.newResponse(Constants.MESSAGE_TYPE_HEART, request.getSerialize());
        response.setSequence(request.getSequence());
        response.setReturn(Constants.VERSION_150);

        return response;
    }

    public static InvocationResponse createScannerHeartResponse(InvocationRequest request) {
        if (request instanceof UnifiedRequest) {
            UnifiedResponse response = null;
            byte serialize = request.getSerialize();
            response = (UnifiedResponse) SerializerFactory.getSerializer(serialize).newResponse();
            response.setSequence(request.getSequence());
            response.setSerialize(serialize);
            response.setMessageType(Constants.MESSAGE_TYPE_SCANNER_HEART);
            response.setServiceName(request.getServiceName());
            response.setMethodName(request.getMethodName());
            response.setSeqId(((UnifiedRequest) request).getSeqId());
            response.setCreateMillisTime(System.currentTimeMillis());
            return response;
        } else {
            throw new BadRequestException("invalid scanner heartbeat request");
        }
    }

    public static InvocationResponse createHealthCheckResponse(ProviderContext invocationContext) {
        InvocationRequest request = invocationContext.getRequest();
        InvocationResponse response = InvocationUtils.newResponse(Constants.MESSAGE_TYPE_HEALTHCHECK, request.getSerialize());
        response.setSequence(request.getSequence());
        Map<String, Object> info = new HashMap<String, Object>();
        info.put("version", VersionUtils.VERSION);
        info.put("group", configManager.getGroup());
        info.put("env", configManager.getEnv());
        //check service exists
        info.put("serviceCheck", Boolean.FALSE);
        String serviceName = request.getServiceName();
        if (StringUtils.isNotBlank(serviceName)) {
            Map<String, ProviderConfig<?>> serviceCache = ServicePublisher.getAllServiceProviders();
            ProviderConfig providerConfig = serviceCache.get(serviceName);
            if (providerConfig != null
                    && invocationContext.getChannel().getPort() == providerConfig.getServerConfig().getActualPort()) {
                info.put("serviceCheck", Boolean.TRUE);
            }
        }

        response.setReturn(info);
        return response;
    }

    public static InvocationResponse createHealthCheckResponse(InvocationRequest request) {
        InvocationResponse response = InvocationUtils.newResponse(Constants.MESSAGE_TYPE_HEALTHCHECK, request.getSerialize());
        response.setSequence(request.getSequence());
        Map<String, Object> info = new HashMap<String, Object>();
        info.put("version", VersionUtils.VERSION);
        info.put("group", configManager.getGroup());
        info.put("env", configManager.getEnv());
        response.setReturn(info);

        return response;
    }

    public static String getRequestDetailInfo(String title, ProviderContext providerContext, InvocationRequest request) {
        StringBuilder msg = new StringBuilder();
        msg.append(title).append(", from:")
                .append(providerContext.getChannel() == null ? "" : providerContext.getChannel().getRemoteAddress())
                .append(", to:").append(ConfigManagerLoader.getConfigManager().getLocalIp())
                .append(", time:").append(System.currentTimeMillis()).append("\r\nrequest:")
                .append(request);
        return msg.toString();
    }
}
