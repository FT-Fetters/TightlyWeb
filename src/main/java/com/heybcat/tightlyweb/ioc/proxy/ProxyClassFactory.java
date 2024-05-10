package com.heybcat.tightlyweb.ioc.proxy;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import xyz.ldqc.tightcall.consumer.proxy.factory.ProxyFactory;
import xyz.ldqc.tightcall.consumer.proxy.factory.ProxySupport;
import xyz.ldqc.tightcall.provider.service.ByteBuddyToCglibAdapter;

/**
 * @author Fetters
 */
public class ProxyClassFactory {

    private ProxyClassFactory(){
        throw new UnsupportedOperationException();
    }

    public static Class<?> doByteBuddyProxy(ProxySupport proxySupport){
        return new ByteBuddy()
            .subclass(proxySupport.getTargetClass())
            .method(ElementMatchers.any())
            .intercept(
                MethodDelegation.to(new ByteBuddyToCglibAdapter(proxySupport.getInterceptor()))
            )
            .make()
            .load(ProxyFactory.class.getClassLoader())
            .getLoaded();
    }

}
