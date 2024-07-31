package ioc.filter;

import com.heybcat.tightlyweb.common.ioc.annotation.Cat;
import com.heybcat.tightlyweb.http.chain.RequestFilterChain.FilterContext;
import com.heybcat.tightlyweb.http.common.Response;
import com.heybcat.tightlyweb.http.entity.WebContext;
import com.heybcat.tightlyweb.http.filter.RequestFilter;
import java.util.Objects;

@Cat
public class TestResponseFilter implements RequestFilter {

    @Override
    public void doFilter(WebContext context, FilterContext filterContext) {
        if (context.getRequest().getUri().getPath().equals("/v1/ban/path")){
            filterContext.doResponse(context,
                Response.error("ban url"));
        }
    }
}
