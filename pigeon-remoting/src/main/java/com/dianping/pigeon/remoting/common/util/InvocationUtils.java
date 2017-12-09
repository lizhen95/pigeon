/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.util;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.remoting.invoker.exception.RequestTimeoutException;

public class InvocationUtils {

	private static InvocationBuilder builder;
	
	static {
		builder = ExtensionLoader.getExtension(InvocationBuilder.class);
		if(builder == null) {
			builder = new DefaultInvocationBuilder();
		}
	}
	
	private static ConcurrentHashMap<String, String> remoteCallNameCache = new ConcurrentHashMap<String, String>();

	private static ConfigManager configManager = ConfigManagerLoader.getConfigManager();

	private static final int defaultStrMaxLength = configManager.getIntValue(Constants.KEY_STRING_MAXLENGTH,
			Constants.DEFAULT_STRING_MAXLENGTH);

	private static final int defaultStrMaxItems = configManager.getIntValue(Constants.KEY_STRING_MAXITEMS,
			Constants.DEFAULT_STRING_MAXITEMS);

	public static String toJsonString(Object obj) {
		return StringizerUtils.forJson().from(obj, defaultStrMaxLength, defaultStrMaxItems);
	}

	public static String toJsonString(Object obj, int strMaxLength, int strMaxItems) {
		return StringizerUtils.forJson().from(obj, strMaxLength, strMaxItems);
	}

	public static String getRemoteCallFullName(String methodName, Object[] parameters) {
		if (parameters != null) {
			StringBuilder str = new StringBuilder(methodName).append("(");
			for (int i = 0; i < parameters.length; i++) {
				if (parameters[i] == null) {
					str.append("null,");
				} else {
					str.append(parameters[i].getClass().getName()).append(",");
				}
			}
			if (parameters.length > 0) {
				str.deleteCharAt(str.length() - 1);
			}
			str.append(")");
			return str.toString();
		} else {
			return methodName;
		}
	}

	public static String getRemoteCallFullName(String serviceName, String methodName, Class<?>[] parameterTypes) {
		if (parameterTypes != null) {
			String[] parameterTypes_ = new String[parameterTypes.length];
			for (int i = 0; i < parameterTypes.length; i++) {
				parameterTypes_[i] = parameterTypes[i].getSimpleName();
			}
			return getRemoteCallFullName(serviceName, methodName, parameterTypes_);
		} else {
			return getRemoteCallFullName(serviceName, methodName, new String[0]);
		}
	}

	public static String getRemoteCallFullName(String serviceName, String methodName, String[] parameterTypes) {
		String cacheKey = new StringBuilder(serviceName).append("#").append(methodName).append("#")
				.append(StringUtils.join(parameterTypes, "#")).toString();
		String name = remoteCallNameCache.get(cacheKey);
		if (name == null) {
			List<String> serviceFrags = SplitterUtils.by("/").noEmptyItem().split(serviceName);
			int fragLenght = serviceFrags.size();
			name = "Unknown";
			StringBuilder sb = new StringBuilder(128);
			if (fragLenght > 2) {
				sb.append(serviceFrags.get(fragLenght - 2)).append(':').append(serviceFrags.get(fragLenght - 1))
						.append(':').append(methodName);
			} else {
				sb.append(serviceName).append(':').append(methodName);
			}
			sb.append('(');
			int pLen = parameterTypes.length;
			for (int i = 0; i < pLen; i++) {
				String parameter = parameterTypes[i];
				int idx = parameter.lastIndexOf(".");
				if (idx > -1) {
					parameter = parameter.substring(idx + 1);
				}
				sb.append(parameter);
				if (i < pLen - 1) {
					sb.append(',');
				}
			}
			sb.append(')');
			name = sb.toString();

			remoteCallNameCache.putIfAbsent(cacheKey, name);
		}
		return name;
	}
	
	public static InvocationRequest newRequest() {
		return builder.newRequest();
	}
	
	public static InvocationRequest newRequest(InvokerContext invokerContext) {
		return builder.newRequest(invokerContext);
	}

	public static InvocationRequest newRequest(String serviceName, String methodName, Object[] parameters, byte serialize, int messageType,
			int timeout, Class<?>[] parameterClasses) {
		return builder.newRequest(serviceName, methodName, parameters, serialize, messageType, timeout, parameterClasses);
	}
	
	public static InvocationRequest newRequest(String serviceName, String methodName, Object[] parameters, byte serialize, int messageType,
            int timeout, int callType, long seq) {
		return builder.newRequest(serviceName, methodName, parameters, serialize, messageType, timeout, callType, seq);
	}
	
	public static Class<? extends InvocationRequest> getRequestClass() {
		return builder.getRequestClass();
	}
	
	public static InvocationResponse newResponse() {
		return builder.newResponse();
	}
	
	public static InvocationResponse newResponse(int messageType, byte serialize) {
		return builder.newResponse(messageType, serialize);
	}
	
	public static InvocationResponse newResponse(byte serialize, long seq, int messageType, Object returnVal) {
		return builder.newResponse(serialize, seq, messageType, returnVal);
	}
	
	public static Class<? extends InvocationResponse> getResponseClass() {
		return builder.getResponseClass();
	}
	
	public static RequestTimeoutException newTimeoutException(String message) {
		return builder.newTimeoutException(message);
	}
	
}
