package com.heybcat.tightlyweb.http.chain;

import com.heybcat.tightlyweb.http.common.Response;
import com.heybcat.tightlyweb.http.core.WebDispatcher;
import com.heybcat.tightlyweb.http.entity.EndpointDefinition;
import com.heybcat.tightlyweb.http.entity.RouteDefinition;
import com.heybcat.tightlyweb.http.entity.WebTargetMethodDefinition;
import java.lang.reflect.Method;
import java.nio.channels.Channel;
import java.nio.channels.SocketChannel;
import xyz.ldqc.tightcall.chain.ChannelChainGroup;

/**
 * @author Fetters
 */
public class DispatcherRoutingChain extends AbstractTransitiveInBoundChain{

    private final WebDispatcher webDispatcher;

    private final ChannelChainGroup chainGroup;

    public DispatcherRoutingChain(WebDispatcher webDispatcher, ChannelChainGroup chainGroup){
        this.webDispatcher = webDispatcher;
        this.chainGroup = chainGroup;
    }

    @Override
    public void doHandler(Channel channel, Object o) {
        RouteDefinition routeDefinition = (RouteDefinition) o;
        EndpointDefinition dispatch = webDispatcher.dispatch(routeDefinition.getUri().getPath());
        if (dispatch == null){
            chainGroup.doOutBoundChain((SocketChannel) channel, Response.notFound());
            return;
        }
        Method method = dispatch.getMethod(routeDefinition.getUri().getPath(),
            routeDefinition.getRequestMethod());
        if (method != null){
            WebTargetMethodDefinition webTargetMethodDefinition = new WebTargetMethodDefinition(
                routeDefinition.getRequest(), method, dispatch.getEndpointObject());
            next(channel, webTargetMethodDefinition);
        }else {
            chainGroup.doOutBoundChain((SocketChannel) channel, Response.methodNotAllowed("Wrong method"));
            return;
        }
    }
}
