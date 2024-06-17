package com.heybcat.tightlyweb.http.core;

import com.heybcat.tightlyweb.common.util.FileUtil;
import com.heybcat.tightlyweb.common.util.UrlUtil;
import com.heybcat.tightlyweb.http.annotation.WebEndpoint;
import com.heybcat.tightlyweb.http.annotation.WebMapping;
import com.heybcat.tightlyweb.http.entity.EndpointDefinition;
import com.heybcat.tightlyweb.http.entity.MethodDefinition;
import com.heybcat.tightlyweb.http.exception.WebDispatcherInitException;
import com.heybcat.tightlyweb.common.ioc.IocManager;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import xyz.ldqc.tightcall.protocol.http.HttpMethodEnum;
import xyz.ldqc.tightcall.protocol.http.HttpNioRequest;

/**
 * @author Fetters
 */
public class WebDispatcher {

    private final IocManager iocManager;

    private Map<String, EndpointDefinition> endpointDefinitionMap;

    public WebDispatcher(IocManager iocManager) {
        this.iocManager = iocManager;
        initEndpointFromIocManager();
    }

    /**
     * init endpoint from ioc manager
     */
    private void initEndpointFromIocManager() {
        if (iocManager == null) {
            throw new WebDispatcherInitException("iocManager is null");
        }
        // list all cat annotation with @WebEndpoint
        List<Object> webEndpointList = iocManager.listAnnotationWith(WebEndpoint.class);
        endpointDefinitionMap = new ConcurrentHashMap<>(webEndpointList.size());
        // step each web endpoint
        for (Object webEndpoint : webEndpointList) {
            String baseurl = null;
            // class annotation with @WebMapping, get as baseurl
            if (webEndpoint.getClass().isAnnotationPresent(WebMapping.class)) {
                WebMapping webMapping = webEndpoint.getClass().getAnnotation(WebMapping.class);
                baseurl = UrlUtil.trim(webMapping.value());
            }
            Map<String, MethodDefinition> methodMap = new HashMap<>(12);
            Method[] methods = webEndpoint.getClass().getDeclaredMethods();
            // create endpoint definition
            EndpointDefinition endpointDefinition = new EndpointDefinition(baseurl, webEndpoint,
                methodMap);
            // step each method，find method with @WebMapping
            for (Method method : methods) {
                if (method.isAnnotationPresent(WebMapping.class)) {
                    WebMapping webMapping = method.getAnnotation(WebMapping.class);
                    String methodUrl = UrlUtil.trim(webMapping.value());
                    methodMap.put(methodUrl,
                        new MethodDefinition(method, List.of(webMapping.method())));
                    endpointDefinitionMap.put(baseurl + methodUrl, endpointDefinition);

                }
            }
        }
    }


    /**
     * dispatch url to endpoint
     *
     * @param url url
     * @return endpoint
     */
    public EndpointDefinition dispatch(String url) {
        return endpointDefinitionMap.get(url);
    }

    public void enableResourceMapping(String resourcePath) {
        ResourceDispatchHandler resourceDispatchHandler = new ResourceDispatchHandler();
        Class<?> bootClass = iocManager.getBootClass();
        if (resourcePath.startsWith("/")) {
            resourcePath = resourcePath.substring(1);
        }
        List<String> fileList = FileUtil.listResourceFiles(bootClass, resourcePath);
        URL resource = bootClass.getClassLoader().getResource(resourcePath);
        if (resource == null){
            return;
        }
        Method dispatchMethod;
        MethodDefinition methodDefinition;
        try {
             dispatchMethod = ResourceDispatchHandler.class.getMethod("dispatch",
                HttpNioRequest.class);
             methodDefinition = new MethodDefinition(dispatchMethod,
                List.of(HttpMethodEnum.GET));
        } catch (NoSuchMethodException e) {
            return;
        }

        for (String filePath : fileList) {
            Map<String, MethodDefinition> map = new HashMap<>(1);
            String relatePath = filePath.replace(resourcePath, "").replace("\\", "/");
            map.put(relatePath, methodDefinition);
            endpointDefinitionMap.put("/" + filePath.replace("\\", "/"),
                new EndpointDefinition("/" + resourcePath, resourceDispatchHandler, map));
        }

    }


}
