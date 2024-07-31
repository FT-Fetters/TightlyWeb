package com.heybcat.tightlyweb.http.chain;

import com.alibaba.fastjson2.JSON;
import com.heybcat.tightlyweb.http.entity.ResourceDispatchResult;
import com.heybcat.tightlyweb.http.entity.WebContext;
import com.heybcat.tightlyweb.http.exception.WebInvokeMethodException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.Channel;
import xyz.ldqc.tightcall.protocol.http.ContentTypeEnum;
import xyz.ldqc.tightcall.protocol.http.HttpCodeEnum;
import xyz.ldqc.tightcall.protocol.http.HttpNioResponse;
import xyz.ldqc.tightcall.protocol.http.HttpVersionEnum;

/**
 * @author Fetters
 */
public class InvokeTargetChain extends AbstractTransitiveInBoundChain{

    @Override
    public void doHandler(Channel channel, Object o) {
        WebContext webContext = (WebContext) o;
        Method targetMethod = webContext.getTargetMethod();
        try {
            Object ret = targetMethod.invoke(webContext.getEndpointObject(), webContext.getArgs());
            // judge ret is ResourceDispatchResult
            if (ResourceDispatchResult.class.isAssignableFrom(ret.getClass())) {
                ResourceDispatchResult resourceDispatchResult = (ResourceDispatchResult) ret;
                next(channel, resourceDispatchResult2Response(resourceDispatchResult));
                return;
            }
            HttpNioResponse response = webContext.getResponse();
            if (response == null){
                response = HttpNioResponse.builder()
                    .contentType(ContentTypeEnum.APPLICATION_JSON)
                    .code(HttpCodeEnum.OK.getCode())
                    .msg(HttpCodeEnum.OK.getMsg())
                    .version(HttpVersionEnum.HTTP_1_1)
                    .build();
            }
            response.write(JSON.toJSONString(ret));
            next(channel, response);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new WebInvokeMethodException("invoke fail", e);
        }
    }

    private HttpNioResponse resourceDispatchResult2Response(ResourceDispatchResult resourceDispatchResult) {
        String resourceContentType = resourceDispatchResult.getContentType();
        ContentTypeEnum contentType = ContentTypeEnum.getContentType(resourceContentType);
        if (contentType == null){
            contentType = ContentTypeEnum.TEXT_PLAIN;
        }
        HttpNioResponse response = HttpNioResponse.builder()
            .contentType(contentType)
            .code(HttpCodeEnum.OK.getCode())
            .msg(HttpCodeEnum.OK.getMsg())
            .version(HttpVersionEnum.HTTP_1_1)
            .build();
        response.write(resourceDispatchResult.getData());
        return response;
    }
}
