package com.dianping.pigeon.remoting.common.domain.generic;

import com.dianping.pigeon.remoting.common.util.Constants;

import java.io.Serializable;
import java.util.Map;

/**
 * @author qi.yin
 *         2016/05/12  下午4:29.
 */
public class GenericResponse implements UnifiedResponse {

    private static final long serialVersionUID = -1L;

    private transient byte serialize;

    private byte protocolVersion = 1;

    private long seq;

    private int messageType;

    private transient String serviceName;

    private transient String methodName;

    private transient Class<?> serviceInterface;

    private Object returnVal;

    private int compressType;

    private transient int size;

    private transient long createMillisTime;

    private Map<String, String> globalContext = null;

    private Map<String, String> localContext = null;

    private int seqId;

    private int port;

    public GenericResponse() {

    }

    public GenericResponse(int messageType, byte serialize) {
        this.messageType = messageType;
        this.serialize = serialize;
    }

    public GenericResponse(byte serialize, long seq, int messageType, Object returnVal) {
        this.serialize = serialize;
        this.seq = seq;
        this.messageType = messageType;
        this.returnVal = returnVal;
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

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public int getMessageType() {
        return this.messageType;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?> getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(Class<?> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    public String getCause() {
        throw new UnsupportedOperationException("operation not supported.");
    }

    public Object getReturn() {
        return this.returnVal;
    }

    @Override
    public Object getContext() {
        throw new UnsupportedOperationException("operation not supported.");
    }

    @Override
    public void setContext(Object context) {
        throw new UnsupportedOperationException("operation not supported.");
    }

    @Override
    public void setReturn(Object obj) {
        this.returnVal = obj;
    }


    public int getCompressType() {
        return compressType;
    }

    public void setCompressType(int compressType) {
        this.compressType = compressType;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("DefaultResponse[").append("[serialize=").append(serialize).append(", seq=").append(seq).
                append(", messageType=").append(messageType);
        if (!(this.messageType == Constants.MESSAGE_TYPE_SERVICE)) {
            builder.append(", return=").append(returnVal);
        }

        builder.append("]");

        return builder.toString();
    }

    @Override
    public void setSerialize(byte serialize) {
        this.serialize = serialize;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Map<String, Serializable> getResponseValues() {
        throw new UnsupportedOperationException("operation not supported.");
    }

    public void setResponseValues(Map<String, Serializable> responseValues) {
        throw new UnsupportedOperationException("operation not supported.");
    }

    public long getCreateMillisTime() {
        return createMillisTime;
    }

    public void setCreateMillisTime(long createMillisTime) {
        this.createMillisTime = createMillisTime;
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

    public boolean hasException() {
        if (messageType == Constants.MESSAGE_TYPE_EXCEPTION ||
                messageType == Constants.MESSAGE_TYPE_SERVICE_EXCEPTION) {
            return true;
        }
        return false;
    }

    @Override
    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public int getSeqId() {
        return seqId;
    }

    @Override
    public void setSeqId(int seqId) {
        this.seqId = seqId;
    }
}
