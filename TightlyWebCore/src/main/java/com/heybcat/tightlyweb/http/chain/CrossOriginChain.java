package com.heybcat.tightlyweb.http.chain;

import com.heybcat.tightlyweb.config.TightlyWebConfigEntity;
import java.nio.channels.Channel;
import xyz.ldqc.tightcall.chain.Chain;
import xyz.ldqc.tightcall.chain.OutboundChain;
import xyz.ldqc.tightcall.protocol.http.HttpNioResponse;

/**
 * @author Fetters
 */
public class CrossOriginChain implements OutboundChain {

    private Chain nextChain;

    private final TightlyWebConfigEntity config;

    public CrossOriginChain(TightlyWebConfigEntity config) {
        this.config = config;
    }


    @Override
    public void doChain(Channel channel, Object o) {
        if (Boolean.TRUE.equals(!config.getCrossOriginEnable()) || !(o instanceof HttpNioResponse)) {
            nextChain.doChain(channel, o);
            return;
        }

        HttpNioResponse response = (HttpNioResponse) o;
        response.addHeader("Access-Control-Allow-Origin", config.getCrossOriginAllowOrigin());
        response.addHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        response.addHeader("Access-Control-Allow-Headers", "Content-Type,X-Requested-With,Authorization");
        response.addHeader("Access-Control-Allow-Credentials", "true");
        response.addHeader("Access-Control-Max-Age", "3600");
        nextChain.doChain(channel, o);
    }

    @Override
    public void setNextChain(Chain chain) {
        this.nextChain = chain;
    }
}
