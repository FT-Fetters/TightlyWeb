package com.heybcat.tightlyweb.http.entity;

import java.net.URI;
import lombok.Getter;
import lombok.Setter;
import xyz.ldqc.tightcall.protocol.http.HttpMethodEnum;
import xyz.ldqc.tightcall.protocol.http.HttpNioRequest;

/**
 * @author Fetters
 */
@Getter
@Setter
public class RouteDefinition {

    private URI uri;

    private HttpMethodEnum method;

    private HttpNioRequest request;


    public RouteDefinition(URI uri, HttpMethodEnum method, HttpNioRequest request) {
        this.uri = uri;
        this.method = method;
        this.request = request;
    }
}
