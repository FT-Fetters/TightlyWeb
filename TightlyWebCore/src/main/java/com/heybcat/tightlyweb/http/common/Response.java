package com.heybcat.tightlyweb.http.common;

import com.alibaba.fastjson2.JSON;
import java.util.HashMap;
import java.util.Map;
import xyz.ldqc.tightcall.protocol.http.ContentTypeEnum;
import xyz.ldqc.tightcall.protocol.http.HttpCodeEnum;
import xyz.ldqc.tightcall.protocol.http.HttpNioResponse;
import xyz.ldqc.tightcall.protocol.http.HttpVersionEnum;

/**
 * @author Fetters
 */
public class Response {

    private Response() {
    }

    public static HttpNioResponse error(String content) {
        Map<String, String> body = new HashMap<>(1);
        body.put("msg", content);
        return HttpNioResponse.builder()
            .code(HttpCodeEnum.INTERNAL_SERVER_ERROR.getCode())
            .version(HttpVersionEnum.HTTP_1_1)
            .contentType(ContentTypeEnum.APPLICATION_JSON)
            .msg(HttpCodeEnum.INTERNAL_SERVER_ERROR.getMsg())
            .body(JSON.toJSONBytes(body))
            .build();
    }

    public static HttpNioResponse notFound() {
        Map<String, String> body = new HashMap<>(1);
        body.put("msg", "404 Not Found");
        return HttpNioResponse.builder()
            .code(HttpCodeEnum.NOT_FOUND.getCode())
            .version(HttpVersionEnum.HTTP_1_1)
            .contentType(ContentTypeEnum.APPLICATION_JSON)
            .msg(HttpCodeEnum.NOT_FOUND.getMsg())
            .body(JSON.toJSONBytes(body))
            .build();
    }

    public static HttpNioResponse okJson(Object body) {
        byte[] bodyBytes = JSON.toJSONBytes(body);
        return HttpNioResponse.builder()
            .code(HttpCodeEnum.OK.getCode())
            .version(HttpVersionEnum.HTTP_1_1)
            .contentType(ContentTypeEnum.APPLICATION_JSON)
            .msg(HttpCodeEnum.OK.getMsg())
            .body(bodyBytes)
            .build();
    }

    public static HttpNioResponse methodNotAllowed(String content) {
        Map<String, String> body = new HashMap<>(1);
        body.put("msg", content);
        return HttpNioResponse.builder()
            .code(HttpCodeEnum.METHOD_NOT_ALLOWED.getCode())
            .version(HttpVersionEnum.HTTP_1_1)
            .contentType(ContentTypeEnum.APPLICATION_JSON)
            .msg(HttpCodeEnum.METHOD_NOT_ALLOWED.getMsg())
            .body(JSON.toJSONBytes(body))
            .build();
    }

}
