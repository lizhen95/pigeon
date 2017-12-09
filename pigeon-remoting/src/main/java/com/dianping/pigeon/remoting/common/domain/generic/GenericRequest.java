package com.dianping.pigeon.remoting.common.domain.generic;


import java.io.Serializable;
import java.util.Map;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.remoting.common.domain.*;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.common.util.InvocationUtils;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;

/**
 * @author qi.yin
 *         2016/05/12  下午4:28.
 */
public class GenericRequest implements UnifiedRequest {

    private static final long serialVersionUID = -1L;

    private transient byte serialize;

    private byte protocolVersion = 1;

    private long seq;

    private int callType;

    private int messageType;

    private int compressType;

    private int timeout;

    private transient long createMillisTime;

    private String serviceName;

    private transient Class<?> serviceInterface;

    private String methodName;

    private Object[] parameters;

    private Class<?>[] parameterTypes;

    private String app = ConfigManagerLoader.getConfigManager().getAppName();

    private transient int size;

    private Map<String, String> globalContext = null;

    private Map<String, String> localContext = null;

    private String version;

    private int seqId;

    private String clientIp;

    public GenericRequest(String serviceName, String methodName, Object[] parameters, byte serialize, int messageType,
                          int timeout) {
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.parameters = parameters;
        this.serialize = serialize;
        this.messageType = messageType;
        this.timeout = timeout;
    }


    public GenericRequest() {
    }

    public GenericRequest(InvokerContext invokerContext) {
        if (invokerContext != null) {
            InvokerConfig<?> invokerConfig = invokerContext.getInvokerConfig();
            if (invokerConfig != null) {
                this.serviceName = invokerConfig.getUrl();
                this.serialize = invokerConfig.getSerialize();
                this.timeout = invokerConfig.getTimeout(invokerContext.getMethodName());
                this.setVersion(invokerConfig.getVersion());
                if (CallMethod.isOneway(invokerConfig.getCallType())) {
                    this.setCallType(com.dianping.pigeon.remoting.common.domain.CallType.NOREPLY.getCode());
                } else {
                    this.setCallType(com.dianping.pigeon.remoting.common.domain.CallType.REPLY.getCode());
                }
            }
            this.methodName = invokerContext.getMethodName();
            this.parameters = invokerContext.getArguments();
            this.messageType = Constants.MESSAGE_TYPE_SERVICE;
        }
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public byte getSerialize() {
        return this.serialize;
    }

    public void setSequence(long seq) {
        this.seq = seq;
    }

    public long getSequence() {
        return this.seq;
    }

    public byte getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(byte protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    @Override
    public Object getContext() {
        throw new UnsupportedOperationException("operation not supported.");
    }

    @Override
    public void setContext(Object context) {
        throw new UnsupportedOperationException("operation not supported.");
    }

    public void setCallType(int callType) {
        this.callType = callType;
    }

    public int getCallType() {
        return this.callType;
    }

    public int getTimeout() {
        return this.timeout;
    }

    public long getCreateMillisTime() {
        return this.createMillisTime;
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public Class<?> getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(Class<?> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    public String getMethodName() {
        return this.methodName;
    }

    public String[] getParamClassName() {
        if (this.parameters == null) {
            return new String[0];
        }
        String[] paramClassNames = new String[this.parameters.length];

        int k = 0;
        for (Object parameter : this.parameters) {
            if (parameter == null) {
                paramClassNames[k] = "NULL";
            } else {
                paramClassNames[k] = this.parameters[k].getClass().getName();
            }
            k++;
        }
        return paramClassNames;
    }

    public Object[] getParameters() {
        return this.parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public int getMessageType() {
        return this.messageType;
    }

    public int getCompressType() {
        return compressType;
    }

    public void setCompressType(int compressType) {
        this.compressType = compressType;
    }

    @Override
    public void setCreateMillisTime(long createTime) {
        this.createMillisTime = createTime;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("DefaultRequest[serialize=").append(serialize).append(", seq=").append(seq).append(", msgType=").
                append(messageType).append(", callType=").append(callType).append(", timeout=").append(timeout).
                append(", url=").append(serviceName).append(", method=").append(methodName).append(", app=").append(app).
                append(", created=").append(createMillisTime);

        if (Constants.LOG_PARAMETERS) {
            builder.append(", parameters=").append(InvocationUtils.toJsonString(parameters));
        }

        builder.append("]");

        return builder.toString();
    }

    @Override
    public void setSerialize(byte serialize) {
        this.serialize = serialize;
    }

    @Override
    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    @Override
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Map<String, Serializable> getGlobalValues() {
        throw new UnsupportedOperationException("operation not supported.");
    }

    public void setGlobalValues(Map<String, Serializable> globalValues) {
        throw new UnsupportedOperationException("operation not supported.");
    }

    public Map<String, Serializable> getRequestValues() {
        throw new UnsupportedOperationException("operation not supported.");
    }

    public void setRequestValues(Map<String, Serializable> requestValues) {
        throw new UnsupportedOperationException("operation not supported.");
    }

    public Map<String, String> getGlobalContext() {
        return globalContext;
    }

    public void setGlobalContext(Map<String, String> globalContext) {
        this.globalContext = globalContext;
    }

    public Map<String, String> getLocalContext() {
        return localContext;
    }

    public void setLocalContext(Map<String, String> localContext) {
        this.localContext = localContext;
    }

    @Override
    public int getSeqId() {
        return seqId;
    }

    @Override
    public void setSeqId(int seqId) {
        this.seqId = seqId;
    }

    @Override
    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    @Override
    public String getClientIp() {
        return clientIp;
    }
}
