package com.heybcat.tightlyweb.http.chain;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.heybcat.tightlyweb.common.util.TypeUtil;
import com.heybcat.tightlyweb.http.annotation.Body;
import com.heybcat.tightlyweb.http.entity.WebContext;
import com.heybcat.tightlyweb.http.entity.WebTargetMethodDefinition;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.channels.Channel;
import java.util.Map;
import xyz.ldqc.tightcall.protocol.http.ContentTypeEnum;
import xyz.ldqc.tightcall.protocol.http.HttpCodeEnum;
import xyz.ldqc.tightcall.protocol.http.HttpNioRequest;
import xyz.ldqc.tightcall.protocol.http.HttpNioResponse;
import xyz.ldqc.tightcall.protocol.http.HttpVersionEnum;

/**
 * @author Fetters
 */
public class ParseRequestToMethodParameterChain extends AbstractTransitiveInBoundChain {


    @Override
    public void doHandler(Channel channel, Object o) {
        WebTargetMethodDefinition webTargetMethodDefinition = (WebTargetMethodDefinition) o;
        HttpNioRequest request = webTargetMethodDefinition.getRequest();
        HttpNioResponse response = HttpNioResponse.builder()
            .contentType(ContentTypeEnum.APPLICATION_JSON)
            .code(HttpCodeEnum.OK.getCode())
            .msg(HttpCodeEnum.OK.getMsg())
            .version(HttpVersionEnum.HTTP_1_1).build();
        Method method = webTargetMethodDefinition.getMethod();
        // get method parameters
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        // get request param map
        Map<String, String> param = request.getParam();
        String body = request.getBody();
        for (int i = 0; i < parameters.length; i++) {
            args[i] = getArg(parameters[i], param, body, request, response);
        }
        WebContext webContext = new WebContext(request, response, args, method,
            webTargetMethodDefinition.getEndpointObject());
        next(channel, webContext);
    }

    private Object getArg(Parameter parameter, Map<String, String> param, String body,
        HttpNioRequest request, HttpNioResponse response) {
        if (isRequest(parameter)) {
            return request;
        }
        if (isResponse(parameter)) {
            return response;
        }
        String name = parameter.getName();
        if (param.containsKey(name)) {
            return TypeUtil.parse2TargetType(param.get(name), parameter.getType());
        }

        if (parameter.isAnnotationPresent(Body.class)) {
            try {
                return JSON.parseObject(body, JSONObject.class)
                    .getObject(name, parameter.getType());
            } catch (Exception e) {
                return TypeUtil.parse2TargetType(body, parameter.getType());
            }
        }
        return null;
    }

    private boolean isRequest(Parameter parameter) {
        return parameter.getType().equals(HttpNioRequest.class);
    }

    private boolean isResponse(Parameter parameter) {
        return parameter.getType().equals(HttpNioResponse.class);
    }


}
