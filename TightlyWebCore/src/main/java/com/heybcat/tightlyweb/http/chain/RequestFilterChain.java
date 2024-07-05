package com.heybcat.tightlyweb.http.chain;

import com.heybcat.tightlyweb.annoation.ConfigValue;
import com.heybcat.tightlyweb.common.ioc.IocManager;
import com.heybcat.tightlyweb.http.entity.WebContext;
import com.heybcat.tightlyweb.http.filter.RequestFilter;
import java.nio.channels.Channel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import xyz.ldqc.tightcall.chain.ChannelChainGroup;
import xyz.ldqc.tightcall.protocol.http.HttpNioResponse;

/**
 * @author Fetters
 */
public class RequestFilterChain extends AbstractTransitiveInBoundChain {

    private static final List<RequestFilter> REQUEST_FILTERS = new ArrayList<>();

    private final ChannelChainGroup chainGroup;

    public RequestFilterChain(IocManager iocManager, ChannelChainGroup chainGroup) {
        REQUEST_FILTERS.clear();
        REQUEST_FILTERS.addAll(iocManager.listInterfaceWith(RequestFilter.class));
        this.chainGroup = chainGroup;
    }


    @Override
    public void doHandler(Channel channel, Object o) {
        if (WebContext.class.isAssignableFrom(o.getClass())) {
            WebContext webContext = (WebContext) o;
            FilterContext.CONTEXT_THREAD_LOCAL.set(0);
            REQUEST_FILTERS.get(0).doFilter(webContext, FilterContext.INSTANCE);

            Integer index = FilterContext.CONTEXT_THREAD_LOCAL.get();
            if (index == null){
                chainGroup.doOutBoundChain((SocketChannel) channel, webContext.getResponse());
            }else {
                FilterContext.CONTEXT_THREAD_LOCAL.remove();
                next(channel, o);
            }

        } else {
            next(channel, o);
        }
    }

    public static class FilterContext {

        private static final ThreadLocal<Integer> CONTEXT_THREAD_LOCAL = new ThreadLocal<>();

        private static final FilterContext INSTANCE = new FilterContext();

        private FilterContext() {
        }

        public void next(WebContext webContext) {
            Integer cur = CONTEXT_THREAD_LOCAL.get();
            if (cur == null) {
                return;
            }
            if (cur + 1 < REQUEST_FILTERS.size()) {
                REQUEST_FILTERS.get(cur + 1).doFilter(webContext, this);
                if (CONTEXT_THREAD_LOCAL.get() != null){
                    CONTEXT_THREAD_LOCAL.set(CONTEXT_THREAD_LOCAL.get() + 1);
                }
            }
        }

        public void doResponse(WebContext webContext, HttpNioResponse response) {
            webContext.setResponse(response);
            CONTEXT_THREAD_LOCAL.remove();
        }
    }
}
