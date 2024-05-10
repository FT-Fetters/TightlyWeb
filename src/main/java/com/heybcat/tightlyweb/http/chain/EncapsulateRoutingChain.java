package com.heybcat.tightlyweb.http.chain;

import com.heybcat.tightlyweb.http.entity.RouteDefinition;
import java.nio.channels.Channel;
import xyz.ldqc.tightcall.protocol.http.HttpNioRequest;

/**
 * 封装路由
 * @author Fetters
 */
public class EncapsulateRoutingChain extends AbstractTransitiveInBoundChain{

    @Override
    public void doHandler(Channel channel, Object o) {
        HttpNioRequest request = (HttpNioRequest) o;
        RouteDefinition routeDefinition = new RouteDefinition(request.getUri(),
            request.getDefinedMethod(), request);
        next(channel, routeDefinition);
    }
}
