package com.heybcat.tightlyweb.http.chain;

import java.nio.channels.Channel;
import xyz.ldqc.tightcall.chain.Chain;
import xyz.ldqc.tightcall.chain.InboundChain;
import xyz.ldqc.tightcall.server.handler.ChannelHandler;

/**
 * @author Fetters
 */
public abstract class AbstractTransitiveInBoundChain implements InboundChain, ChannelHandler {


  private Chain nextChain;

  @Override
  public void doChain(Channel channel, Object o) {
    doHandler(channel, o);
  }

  @Override
  public void setNextChain(Chain chain) {
    this.nextChain = chain;
  }

  protected void next(Channel channel, Object o){
    if (nextChain != null) {
      nextChain.doChain(channel, o);
    }
  }
}
