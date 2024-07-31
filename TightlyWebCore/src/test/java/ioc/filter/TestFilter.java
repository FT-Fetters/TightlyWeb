package ioc.filter;

import com.heybcat.tightlyweb.common.ioc.annotation.Cat;
import com.heybcat.tightlyweb.http.chain.RequestFilterChain.FilterContext;
import com.heybcat.tightlyweb.http.entity.WebContext;
import com.heybcat.tightlyweb.http.filter.RequestFilter;

@Cat
public class TestFilter implements RequestFilter {

    @Override
    public void doFilter(WebContext context, FilterContext filterContext) {
        System.out.println(context.getRequest().getUri().toString());
        filterContext.next(context);
    }
}
