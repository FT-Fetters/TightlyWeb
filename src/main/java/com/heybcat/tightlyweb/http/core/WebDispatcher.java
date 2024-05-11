package com.heybcat.tightlyweb.http.core;

import com.heybcat.tightlyweb.common.util.UrlUtil;
import com.heybcat.tightlyweb.http.annotation.WebEndpoint;
import com.heybcat.tightlyweb.http.annotation.WebMapping;
import com.heybcat.tightlyweb.http.entity.EndpointDefinition;
import com.heybcat.tightlyweb.http.exception.WebDispatcherInitException;
import com.heybcat.tightlyweb.ioc.IocManager;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Fetters
 */
public class WebDispatcher {

    private final IocManager iocManager;

    private Map<String, EndpointDefinition> endpointDefinitionMap;

    public WebDispatcher(IocManager iocManager){
        this.iocManager = iocManager;
        initEndpointFromIocManager();
    }

    /**
     * init endpoint from ioc manager
     */
    private void initEndpointFromIocManager(){
        if (iocManager == null){
            throw new WebDispatcherInitException("iocManager is null");
        }
        // list all cat annotation with @WebEndpoint
        List<Object> webEndpointList = iocManager.listAnnotationWith(WebEndpoint.class);
        endpointDefinitionMap = new ConcurrentHashMap<>(webEndpointList.size());
        // step each web endpoint
        for (Object webEndpoint : webEndpointList) {
            String baseurl = null;
            // class annotation with @WebMapping, get as baseurl
            if (webEndpoint.getClass().isAnnotationPresent(WebMapping.class)){
                WebMapping webMapping = webEndpoint.getClass().getAnnotation(WebMapping.class);
                baseurl = UrlUtil.trim(webMapping.value());
            }
            Map<String, Method> methodMap = new HashMap<>(12);
            Method[] methods = webEndpoint.getClass().getMethods();
            // create endpoint definition
            EndpointDefinition endpointDefinition = new EndpointDefinition(baseurl, webEndpoint, methodMap);
            // step each method，find method with @WebMapping
            for (Method method : methods) {
                if (method.isAnnotationPresent(WebMapping.class)){
                    WebMapping webMapping = method.getAnnotation(WebMapping.class);
                    String methodUrl = UrlUtil.trim(webMapping.value());
                    methodMap.put(methodUrl, method);
                    endpointDefinitionMap.put(baseurl + methodUrl, endpointDefinition);

                }
            }
        }
    }


    /**
     * dispatch url to endpoint
     * @param url url
     * @return endpoint
     */
    public EndpointDefinition dispatch(String url){
        return endpointDefinitionMap.get(url);
    }






}
