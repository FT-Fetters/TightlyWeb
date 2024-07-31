package com.heybcat.tightlyweb.http.chain;

import com.heybcat.tightlyweb.http.common.Response;
import java.nio.channels.Channel;
import java.nio.channels.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ldqc.tightcall.chain.ChannelChainGroup;
import xyz.ldqc.tightcall.protocol.http.HttpNioResponse;

/**
 * @author Fetters
 */
public class ExceptionCatchChain extends AbstractTransitiveInBoundChain{

    private static final Logger log = LoggerFactory.getLogger(ExceptionCatchChain.class);
    private final ChannelChainGroup chainGroup;

    public ExceptionCatchChain(ChannelChainGroup chainGroup) {
        this.chainGroup = chainGroup;
    }

    @Override
    public void doHandler(Channel channel, Object o) {
        try {
            next(channel, o);
        }catch (Exception e){
            HttpNioResponse serverExceptionError = Response.error("Server exception error");
            chainGroup.doOutBoundChain((SocketChannel) channel, serverExceptionError);
            log.error("Server exception error", e);
        }
    }
}
