package com.heybcat.tightlyweb.ioc.proxy;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import net.sf.cglib.proxy.MethodProxy;
import xyz.ldqc.tightcall.consumer.proxy.factory.ProxyMethodInterceptor;

/**
 * @author Fetters
 */
public class CatProxyMethodInterceptor implements ProxyMethodInterceptor {

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy,
        Callable<?> callable) throws Throwable {
        // TODO finish intercept
        return callable.call();
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy)
        throws Throwable {
        return methodProxy.invoke(o, objects);
    }
}
