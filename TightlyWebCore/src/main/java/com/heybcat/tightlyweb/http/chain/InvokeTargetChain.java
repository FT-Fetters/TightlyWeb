package com.heybcat.tightlyweb.http.chain;

import com.alibaba.fastjson2.JSON;
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
}
