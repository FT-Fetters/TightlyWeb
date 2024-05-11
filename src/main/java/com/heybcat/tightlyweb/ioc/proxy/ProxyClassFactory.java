package com.heybcat.tightlyweb.ioc.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.MethodDelegation;
import xyz.ldqc.tightcall.consumer.proxy.factory.ProxySupport;
import xyz.ldqc.tightcall.provider.service.ByteBuddyToCglibAdapter;

/**
 * @author Fetters
 */
public class ProxyClassFactory {

    private ProxyClassFactory() {
        throw new UnsupportedOperationException();
    }

    public static Class<?> doByteBuddyProxy(ProxySupport proxySupport) {
        ByteBuddy byteBuddy = new ByteBuddy();
        Builder<?> builder = byteBuddy.subclass(proxySupport.getTargetClass());
        // 复制类注解
        for (Annotation annotation : proxySupport.getTargetClass().getAnnotations()) {
            builder = builder.annotateType(annotation);
        }
        ByteBuddyToCglibAdapter adapter = new ByteBuddyToCglibAdapter(
            proxySupport.getInterceptor());

        for (Method declaredMethod : proxySupport.getTargetClass().getDeclaredMethods()) {
            builder = builder.defineMethod(declaredMethod.getName(), declaredMethod.getReturnType(),
                    declaredMethod.getModifiers())
                .withParameters(declaredMethod.getParameterTypes())
                .intercept(MethodDelegation.to(adapter))
                .annotateMethod(declaredMethod.getAnnotations());
        }
        return builder
            .make()
            .load(ProxyClassFactory.class.getClassLoader())
            .getLoaded();
    }

}
