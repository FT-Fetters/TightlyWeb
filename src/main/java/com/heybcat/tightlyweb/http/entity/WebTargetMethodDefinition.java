package com.heybcat.tightlyweb.http.entity;

import java.lang.reflect.Method;
import lombok.Getter;
import xyz.ldqc.tightcall.protocol.http.HttpNioRequest;

/**
 * @author Fetters
 */
@Getter
public class WebTargetMethodDefinition {

    private final HttpNioRequest request;

    private final Method method;

    private final Object endpointObject;


    public WebTargetMethodDefinition(HttpNioRequest request, Method method, Object endpointObject) {
        this.request = request;
        this.method = method;
        this.endpointObject = endpointObject;
    }

}
