package com.heybcat.tightlyweb.http.chain;

import java.nio.channels.Channel;
import java.util.HashMap;
import java.util.Map;
import xyz.ldqc.tightcall.protocol.http.HttpNioRequest;

/**
 * @author Fetters
 */
public class RedirectChain extends AbstractTransitiveInBoundChain{

    private final Map<String, String> redirectMap = new HashMap<>();

    public RedirectChain(String redirect){
        if (redirect == null){
            return;
        }
        for (String node : redirect.split(",")) {
            String[] origWithTarget = node.split("->");
            if (origWithTarget.length != 2){
                continue;
            }
            redirectMap.put(origWithTarget[0], origWithTarget[1]);
        }
    }

    @Override
    public void doHandler(Channel channel, Object o) {
        if (HttpNioRequest.class.isAssignableFrom(o.getClass())) {
            HttpNioRequest request = (HttpNioRequest) o;
            if (redirectMap.containsKey(request.getUri().getPath())){
                request.resetUri(redirectMap.get(request.getUri().getPath()));
            }
        }
        next(channel, o);
    }
}
