package com.heybcat.tightlyweb.http.entity;

import java.lang.reflect.Method;
import lombok.Getter;
import lombok.Setter;
import xyz.ldqc.tightcall.protocol.http.HttpNioRequest;
import xyz.ldqc.tightcall.protocol.http.HttpNioResponse;

/**
 * @author Fetters
 */
@Getter
@Setter
public class WebContext {

    private final HttpNioRequest request;

    private HttpNioResponse response;

    private final Object[] args;

    private final Method targetMethod;

    private final Object endpointObject;

    public WebContext(HttpNioRequest request, HttpNioResponse response, Object[] args,
        Method targetMethod, Object endpointObject) {
        this.request = request;
        this.response = response;
        this.args = args;
        this.targetMethod = targetMethod;
        this.endpointObject = endpointObject;
    }
}
