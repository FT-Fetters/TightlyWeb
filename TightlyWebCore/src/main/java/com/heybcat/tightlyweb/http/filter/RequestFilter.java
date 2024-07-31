package com.heybcat.tightlyweb.http.filter;

import com.heybcat.tightlyweb.http.chain.RequestFilterChain.FilterContext;
import com.heybcat.tightlyweb.http.entity.WebContext;

/**
 * @author Fetters
 */
public interface RequestFilter {

    void doFilter(WebContext context, FilterContext filterContext);

}
