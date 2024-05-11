package com.heybcat.tightlyweb.http.chain;

import com.heybcat.tightlyweb.http.core.WebDispatcher;
import com.heybcat.tightlyweb.http.entity.EndpointDefinition;
import com.heybcat.tightlyweb.http.entity.RouteDefinition;
import com.heybcat.tightlyweb.http.entity.WebTargetMethodDefinition;
import java.lang.reflect.Method;
import java.nio.channels.Channel;

/**
 * @author Fetters
 */
public class DispatcherRoutingChain extends AbstractTransitiveInBoundChain{

    private final WebDispatcher webDispatcher;

    public DispatcherRoutingChain(WebDispatcher webDispatcher){
        this.webDispatcher = webDispatcher;
    }

    @Override
    public void doHandler(Channel channel, Object o) {
        RouteDefinition routeDefinition = (RouteDefinition) o;
        EndpointDefinition dispatch = webDispatcher.dispatch(routeDefinition.getUri().toString());
        if (dispatch == null){
            // TODO response 404
            return;
        }
        Method method = dispatch.getMethod(routeDefinition.getUri().toString(),
            routeDefinition.getRequestMethod());
        if (method != null){
            WebTargetMethodDefinition webTargetMethodDefinition = new WebTargetMethodDefinition(
                routeDefinition.getRequest(), method);
            next(channel, webTargetMethodDefinition);
        }else {
            // TODO response method not support
        }
    }
}
