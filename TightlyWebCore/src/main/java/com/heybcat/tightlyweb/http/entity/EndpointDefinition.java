package com.heybcat.tightlyweb.http.entity;

import java.lang.reflect.Method;
import java.util.Map;
import lombok.Getter;
import xyz.ldqc.tightcall.protocol.http.HttpMethodEnum;

/**
 * @author Fetters
 */
@Getter
public class EndpointDefinition {

    private final String baseurl;

    private final Object endpointObject;

    Map<String, MethodDefinition> methodMap;

    public EndpointDefinition(String baseurl, Object endpointObject, Map<String, MethodDefinition> methodMap) {
        this.baseurl = baseurl;
        this.endpointObject = endpointObject;
        this.methodMap = methodMap;
    }

    public Method getMethod(String url, HttpMethodEnum method) {
        MethodDefinition methodDefinition = methodMap.get(url.replaceFirst(baseurl, ""));
        if (methodDefinition.getSupportMethod().contains(method)) {
            return methodDefinition.getMethod();
        }else {
            return null;
        }
    }

}
