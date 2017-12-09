/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.config;

import java.util.Map;

import com.dianping.pigeon.remoting.common.codec.SerializerType;
import com.dianping.pigeon.remoting.common.domain.CallMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.concurrent.InvocationCallback;
import com.dianping.pigeon.remoting.invoker.route.balance.LoadBalanceManager;
import com.dianping.pigeon.remoting.invoker.route.region.RegionPolicyManager;
import com.dianping.pigeon.util.ThriftUtils;

public class InvokerConfig<T> {
    private static final Logger logger = LoggerLoader.getLogger(InvokerConfig.class);
    public static final String CALL_SYNC = CallMethod.SYNC.getName();
    public static final String CALL_CALLBACK = CallMethod.CALLBACK.getName();
    public static final String CALL_ONEWAY = CallMethod.ONEWAY.getName();
    public static final String CALL_FUTURE =CallMethod.FUTURE.getName();

    public static final String PROTOCOL_HTTP = Constants.PROTOCOL_HTTP;
    public static final String PROTOCOL_DEFAULT = Constants.PROTOCOL_DEFAULT;

    public static final String SERIALIZE_HESSIAN = SerializerType.HESSIAN.getName();
    public static final String SERIALIZE_JAVA = SerializerType.JAVA.getName();
    public static final String SERIALIZE_PROTO = SerializerType.PROTO.getName();
    public static final String SERIALIZE_JSON = SerializerType.JSON.getName();
    public static final String SERIALIZE_FST = SerializerType.FST.getName();

    private ConfigManager configManager = ConfigManagerLoader.getConfigManager();

    private Class<T> serviceInterface;

    private String url;

    private String version;

    private byte callMethod = CallMethod.SYNC.getCode();

    private String callType = CallMethod.SYNC.getName();

    private byte serialize = SerializerType.HESSIAN.getCode();

    private int timeout = configManager.getIntValue(Constants.KEY_INVOKER_TIMEOUT, Constants.DEFAULT_INVOKER_TIMEOUT);

    private InvocationCallback callback;

    private String suffix = configManager.getGroup();

    private String loadbalance = LoadBalanceManager.DEFAULT_LOADBALANCE;

    private String regionPolicy = RegionPolicyManager.INSTANCE.DEFAULT_REGIONPOLICY;

    private boolean timeoutRetry = false;

    private String cluster = Constants.CLUSTER_FAILFAST;

    private int retries = 1;

    private String vip;

    private int maxRequests = configManager.getIntValue(Constants.KEY_INVOKER_MAXREQUESTS, 0);

    private String protocol = Constants.PROTOCOL_DEFAULT;

    private Map<String, InvokerMethodConfig> methods;

    private ClassLoader classLoader;

    private transient String secret;

    private String remoteAppKey;

    private Object mock;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public Map<String, InvokerMethodConfig> getMethods() {
        return methods;
    }

    public void setMethods(Map<String, InvokerMethodConfig> methods) {
        this.methods = methods;
    }

    public InvokerMethodConfig getMethod(String methodName) {
        if (methods != null && !methods.isEmpty()) {
            return methods.get(methodName);
        }
        return null;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public int getMaxRequests() {
        return maxRequests;
    }

    public void setMaxRequests(int maxRequests) {
        this.maxRequests = maxRequests;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVip() {
        return vip;
    }

    public void setVip(String vip) {
        if (!StringUtils.isBlank(vip)) {
            this.vip = vip.trim();
        }
    }

    public Class<T> getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(Class<T> serviceInterface) {
        if (serviceInterface != null && !serviceInterface.isInterface()) {
            throw new IllegalArgumentException("'serviceInterface' must be an interface");
        }
        this.serviceInterface = serviceInterface;
    }

    public boolean isTimeoutRetry() {
        return timeoutRetry;
    }

    public void setTimeoutRetry(boolean timeoutRetry) {
        this.timeoutRetry = timeoutRetry;
    }

    public String getLoadbalance() {
        return loadbalance;
    }

    public void setLoadbalance(String loadbalance) {
        if (!StringUtils.isBlank(loadbalance)) {
            this.loadbalance = loadbalance.trim();
        }
    }

    public String getRegionPolicy() {
        return regionPolicy;
    }

    public void setRegionPolicy(String regionPolicy) {
        if (StringUtils.isNotBlank(regionPolicy)) {
            this.regionPolicy = regionPolicy;
        }
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        if (!StringUtils.isBlank(cluster)) {
            this.cluster = cluster.trim();
        }
    }

    public int getRetries() {
        return retries;
    }

    public int getRetries(String methodName) {
        InvokerMethodConfig methodConfig = getMethod(methodName);

        if (methodConfig != null && methodConfig.getRetries() > 0) {
            return methodConfig.getRetries();
        }

        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public String getRemoteAppKey() {
        return remoteAppKey;
    }

    public void setRemoteAppKey(String remoteAppKey) {
        this.remoteAppKey = remoteAppKey;
    }

    public InvokerConfig(Class<T> serviceInterface, String url, int timeout, String callMethod, String serialize,
                         InvocationCallback callback, String suffix, boolean writeBufferLimit, String loadbalance, String cluster,
                         int retries, boolean timeoutRetry, String vip, String version, String protocol) {
        this.setServiceInterface(serviceInterface);
        this.setUrl(url);
        this.setTimeout(timeout);
        this.setCallType(callMethod);
        this.setCallback(callback);
        this.setSuffix(suffix);
        this.setCluster(cluster);
        this.setLoadbalance(loadbalance);
        this.setRetries(retries);
        this.setTimeoutRetry(timeoutRetry);
        this.setSerialize(serialize);
        this.setVip(vip);
        this.setVersion(version);
        this.setProtocol(protocol);
    }

    public InvokerConfig(String url, Class<T> serviceInterface) {
        this.setServiceInterface(serviceInterface);
        this.setUrl(url);
    }

    public InvokerConfig(Class<T> serviceInterface) {
        this.setServiceInterface(serviceInterface);
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        if (url != null) {
            url = url.trim();
        }
        this.url = url;
    }

    /**
     * @return the timeout
     */
    public int getTimeout() {
        return timeout;
    }

    public int getTimeout(String methodName) {
        InvokerMethodConfig methodConfig = getMethod(methodName);

        if (methodConfig != null && methodConfig.getTimeout() > 0) {
            return methodConfig.getTimeout();
        }

        return timeout;
    }

    /**
     * @param timeout the timeout to set
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * @return the callType
     */
    public String getCallType() {
        return callType;
    }

    public String getCallType(String methodName) {

        InvokerMethodConfig methodConfig = getMethod(methodName);

        if (methodConfig != null && methodConfig.getCallType() != null) {
            return methodConfig.getCallType();
        }

        return callType;
    }

    /**
     * @param callType the callType to set
     */
    public void setCallType(String callType) {
        if (!CallMethod.isSync(callType) && !CallMethod.isCallback(callType)
                && !CallMethod.isFuture(callType) && !CallMethod.isOneway(callType)) {

            throw new IllegalArgumentException("Pigeon call mode only support[" + CallMethod.SYNC.getName() + ", "
                    + CallMethod.CALLBACK.getName() + ", " + CallMethod.FUTURE.getName() + ", " + CallMethod.ONEWAY.getName() + "].");
        }
        if (!StringUtils.isBlank(callType)) {
            this.callType = callType.trim();
            this.callMethod = CallMethod.getCallMethod(this.callType).getCode();
        }
    }

    public byte getCallMethod(String methodName) {
        InvokerMethodConfig methodConfig = getMethod(methodName);

        if (methodConfig != null && methodConfig.getCallMethod() != 0) {
            return methodConfig.getCallMethod();
        }

        return callMethod;
    }

    public byte getCallMethod() {
        return callMethod;
    }

    public void setCallMethod(byte callMethod) {
        this.callMethod = callMethod;
    }

    /**
     * @return the serialize
     */
    public byte getSerialize() {
        return serialize;
    }

    /**
     * @param serialize the serialize to set
     */
    public void setSerialize(String serialize) {
        if (serialize != null) {
            serialize = serialize.trim();
        }
        this.serialize = SerializerFactory.getSerialize(serialize);

        if (SerializerType.isThrift(this.getSerialize())) {
            if (!ThriftUtils.isSupportedThrift(serviceInterface)) {
                logger.error("Service interface " + serviceInterface.getName() +
                        " do not support thrift serialize, so select default serialize hessian.");
                this.serialize = SerializerType.HESSIAN.getCode();
            }
        }
    }

    /**
     * @return the callback
     */
    public InvocationCallback getCallback() {
        return callback;
    }

    /**
     * @param callback the callback to set
     */
    public void setCallback(InvocationCallback callback) {
        this.callback = callback;
        if (callback != null) {
            setCallType(CallMethod.CALLBACK.getName());
            setCallMethod(CallMethod.CALLBACK.getCode());
        }
    }

    /**
     * @return the suffix
     */
    public String getSuffix() {
        return suffix;
    }

    /**
     * @param suffix the suffix to set
     */
    public void setSuffix(String suffix) {
        if (!StringUtils.isBlank(suffix)) {
            this.suffix = suffix.trim();
        }
    }

    public Object getMock() {
        return mock;
    }

    public void setMock(Object mock) {
        this.mock = mock;
    }

    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
