/**
 *
 */
package com.dianping.pigeon.remoting.invoker.config;

import com.dianping.pigeon.remoting.common.domain.CallMethod;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @author xiangwu
 */
public class InvokerMethodConfig {

    private String name;

    private int actives = 0;

    private int timeout = 0;

    private int retries = -1;

    private String callType;

    private byte callMethod;

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
        this.callMethod = CallMethod.getCallMethod(this.callType).getCode();
    }

    public byte getCallMethod() {
        return callMethod;
    }

    public void setCallMethod(byte callMethod) {
        this.callMethod = callMethod;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getActives() {
        return actives;
    }

    public void setActives(int actives) {
        this.actives = actives;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
