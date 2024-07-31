package proxy;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import net.sf.cglib.proxy.MethodProxy;
import xyz.ldqc.tightcall.consumer.proxy.factory.ProxyMethodInterceptor;

/**
 * @author Fetters
 */
public class InterfaceMethodInterceptor implements ProxyMethodInterceptor {

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy,
        Callable<?> callable) throws Throwable {
//        System.out.println(o);
//        return callable.call();
        return "a";
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy)
        throws Throwable {
//        System.out.println(o);
//        return methodProxy.invoke(o, objects);
        return "a";
    }
}
