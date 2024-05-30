package ioc.cat;

import com.heybcat.tightlyweb.http.annotation.WebEndpoint;
import com.heybcat.tightlyweb.http.annotation.WebMapping;
import com.heybcat.tightlyweb.ioc.annotation.Cat;
import com.heybcat.tightlyweb.ioc.annotation.Inject;
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
        String eat = injectTestObject.getBlackCat().eat();
        return "a = " + a + " eat : " + eat;
    }
}
