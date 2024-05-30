package com.heybcat.tightlyweb.http.entity;

import java.lang.reflect.Method;
import java.util.List;
import lombok.Getter;
import xyz.ldqc.tightcall.protocol.http.HttpMethodEnum;

/**
 * @author Fetters
 */
@Getter
public class MethodDefinition {

    private final Method method;

    private final List<HttpMethodEnum> supportMethod;

    public MethodDefinition(Method method, List<HttpMethodEnum> supportMethod)
    {
        this.method = method;
        this.supportMethod = supportMethod;
    }

}
