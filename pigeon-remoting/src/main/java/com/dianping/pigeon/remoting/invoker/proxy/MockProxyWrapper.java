package com.dianping.pigeon.remoting.invoker.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;

/**
 * Created by chenchongze on 16/8/22.
 */
public class MockProxyWrapper {

    private final Object proxy;

    public MockProxyWrapper(Object proxy) {

        if (proxy == null) {
            throw new IllegalArgumentException("proxy == null");
        }

        this.proxy = proxy;
    }

    public Object invoke(String methodName, Class<?>[] parameterTypes, Object[] arguments)
            throws Throwable {
        Method method = proxy.getClass().getMethod(methodName, parameterTypes);
        try {
            return method.invoke(proxy, arguments);
        }  catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t instanceof UndeclaredThrowableException && t.getCause() != null) {
                throw t.getCause();
            }
            throw t;
        }
    }
}
