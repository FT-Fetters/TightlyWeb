package com.heybcat.tightlyweb.http.chain;

import java.nio.channels.Channel;
import xyz.ldqc.tightcall.protocol.http.HttpNioRequest;

/**
 * @author Fetters
 */
public class FilterHttpRequestChain extends AbstractTransitiveInBoundChain{

    @Override
    public void doHandler(Channel channel, Object o) {
        if (o instanceof HttpNioRequest) {
            next(channel, o);
        }
    }
}
