package com.heybcat.tightlyweb.http.chain.group;

import com.heybcat.tightlyweb.common.ioc.IocManager;
import com.heybcat.tightlyweb.config.TightlyWebConfigEntity;
import com.heybcat.tightlyweb.http.chain.CrossOriginChain;
import com.heybcat.tightlyweb.http.chain.DispatcherRoutingChain;
import com.heybcat.tightlyweb.http.chain.EncapsulateRoutingChain;
import com.heybcat.tightlyweb.http.chain.ExceptionCatchChain;
import com.heybcat.tightlyweb.http.chain.FilterHttpRequestChain;
import com.heybcat.tightlyweb.http.chain.InvokeTargetChain;
import com.heybcat.tightlyweb.http.chain.ParseRequestToMethodParameterChain;
import com.heybcat.tightlyweb.http.chain.RedirectChain;
import com.heybcat.tightlyweb.http.chain.RequestFilterChain;
import com.heybcat.tightlyweb.http.core.WebDispatcher;
import xyz.ldqc.tightcall.chain.support.DefaultChannelChainGroup;

/**
 * @author Fetters
 */
public class HttpChainGroup extends DefaultChannelChainGroup {

    private final WebDispatcher webDispatcher;

    private final TightlyWebConfigEntity config;

    private final IocManager iocManager;

    public HttpChainGroup(WebDispatcher webDispatcher, TightlyWebConfigEntity config, IocManager iocManager) {
        this.webDispatcher = webDispatcher;
        this.config = config;
        this.iocManager = iocManager;
        loadChain();
    }

    private void loadChain(){
        this.addLast(new ExceptionCatchChain(this))
            .addLast(new FilterHttpRequestChain())
            .addLast(new RedirectChain(config.getRedirect()))
            .addLast(new EncapsulateRoutingChain())
            .addLast(new DispatcherRoutingChain(webDispatcher, this))
            .addLast(new ParseRequestToMethodParameterChain())
            .addLast(new RequestFilterChain(iocManager, this))
            .addLast(new InvokeTargetChain())
            .addLast(new CrossOriginChain(config));
    }

}
