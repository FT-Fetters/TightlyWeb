package ioc;

import com.heybcat.tightlyweb.http.core.WebDispatcher;
import com.heybcat.tightlyweb.http.entity.EndpointDefinition;
import com.heybcat.tightlyweb.ioc.IocManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import org.junit.Test;

public class IocTest {

    @Test
    public void testNewIocManager(){
        IocManager iocManager = new IocManager("ioc.cat");
        WebDispatcher webDispatcher = new WebDispatcher(iocManager);
        EndpointDefinition endpointDefinition = webDispatcher.dispatch("/v1/get");
        Map<String, Method> methodMap = endpointDefinition.getMethodMap();
        Method method = methodMap.get("/get");
        try {
            Object invoke = method.invoke(endpointDefinition.getEndpointObject());
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
