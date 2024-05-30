package ioc.cat;

import com.heybcat.tightlyweb.http.annotation.Body;
import com.heybcat.tightlyweb.http.annotation.WebEndpoint;
import com.heybcat.tightlyweb.http.annotation.WebMapping;
import com.heybcat.tightlyweb.ioc.annotation.Cat;
import com.heybcat.tightlyweb.ioc.annotation.Inject;
import entity.PostTestEntity;
import xyz.ldqc.tightcall.protocol.http.HttpMethodEnum;

@Cat
@WebEndpoint
@WebMapping("/v1")
public class WebCat {

    private final InjectTestObject injectTestObject;

    @Inject
    public WebCat(InjectTestObject injectTestObject){
        this.injectTestObject = injectTestObject;
    }

    @WebMapping(value = "/get", method = HttpMethodEnum.GET)
    public String get(int a){
        // ioc注入
        String eat = injectTestObject.getBlackCat().eat();
        return "a = " + a + " eat : " + eat;
    }

    @WebMapping(value = "/post", method = HttpMethodEnum.POST)
    public String post(@Body PostTestEntity entity){
        return "post body: " + entity.getA();
    }
}
