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

    public WebTargetMethodDefinition(HttpNioRequest request, Method method) {
        this.request = request;
        this.method = method;
    }

}
