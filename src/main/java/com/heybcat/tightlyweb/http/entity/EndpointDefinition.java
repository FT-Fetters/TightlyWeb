package com.heybcat.tightlyweb.http.entity;

import java.lang.reflect.Method;
import java.util.Map;
import lombok.Getter;

/**
 * @author Fetters
 */
@Getter
public class EndpointDefinition {

    private final String baseurl;

    private final Object endpointObject;

    Map<String, Method> methodMap;

    public EndpointDefinition(String baseurl, Object endpointObject, Map<String, Method> methodMap) {
        this.baseurl = baseurl;
        this.endpointObject = endpointObject;
        this.methodMap = methodMap;
    }

}
