package com.heybcat.tightlyweb.ioc.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.dynamic.DynamicType.Builder.MethodDefinition.ParameterDefinition.Annotatable;
import net.bytebuddy.dynamic.DynamicType.Builder.MethodDefinition.ParameterDefinition.Initial;
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

            Initial<?> defineMethod = builder.defineMethod(declaredMethod.getName(),
                declaredMethod.getReturnType(),
                declaredMethod.getModifiers());
            if (declaredMethod.getParameterTypes().length > 0) {
                for (int i = 0; i < declaredMethod.getParameterTypes().length; i++) {
                    Annotatable<?> annotatable = defineMethod.withParameter(
                        declaredMethod.getParameterTypes()[i],
                        declaredMethod.getParameters()[i].getName(), i);
                    annotatable.annotateParameter(declaredMethod.getParameterAnnotations()[i]);
                    builder = annotatable.intercept(MethodDelegation.to(adapter))
                        .annotateMethod(declaredMethod.getAnnotations());
                }
            }else{
                builder = defineMethod.intercept(MethodDelegation.to(adapter))
                    .annotateMethod(declaredMethod.getAnnotations());
            }

        }
        return builder
            .make()
            .load(ProxyClassFactory.class.getClassLoader())
            .getLoaded();
    }

}
