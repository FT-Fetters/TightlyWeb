package proxy;

import com.heybcat.tightlyweb.common.ioc.proxy.ProxyClassFactory;
import lombok.SneakyThrows;
import org.junit.Test;
import xyz.ldqc.tightcall.consumer.proxy.factory.ProxyFactory;
import xyz.ldqc.tightcall.consumer.proxy.factory.ProxySupport;

public class BuddyProxyTest {

    @SneakyThrows
    @Test
    public void proxyInterface(){
        ProxySupport proxySupport = new ProxySupport();
        proxySupport.setTargetClass(TargetInterface.class);
        proxySupport.setInterceptor(new InterfaceMethodInterceptor());
        TargetInterface proxy = (TargetInterface) new ProxyFactory().getProxy(proxySupport);
        System.out.println(proxy.call("abc"));

    }


}
