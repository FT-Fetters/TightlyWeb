package com.heybcat.tightlyweb.http;

import com.heybcat.tightlyweb.http.chain.DispatcherRoutingChain;
import com.heybcat.tightlyweb.http.chain.EncapsulateRoutingChain;
import com.heybcat.tightlyweb.http.chain.FilterHttpRequestChain;
import com.heybcat.tightlyweb.http.chain.InvokeTargetChain;
import com.heybcat.tightlyweb.http.chain.ParseRequestToMethodParameterChain;
import com.heybcat.tightlyweb.http.core.WebDispatcher;
import xyz.ldqc.tightcall.chain.support.DefaultChannelChainGroup;

/**
 * @author Fetters
 */
public class HttpChainGroup extends DefaultChannelChainGroup {

    private final WebDispatcher webDispatcher;

    public HttpChainGroup(WebDispatcher webDispatcher) {
        this.webDispatcher = webDispatcher;
        loadChain();
    }

    private void loadChain(){
        this.addLast(new FilterHttpRequestChain())
            .addLast(new EncapsulateRoutingChain())
            .addLast(new DispatcherRoutingChain(webDispatcher, this))
            .addLast(new ParseRequestToMethodParameterChain())
            .addLast(new InvokeTargetChain());
    }

}
