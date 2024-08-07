package ioc.cat;

import com.heybcat.tightlyweb.common.ioc.annotation.Cat;
import com.heybcat.tightlyweb.common.ioc.annotation.Inject;

@Cat
public class InjectTestObject {
    private final BlackCat blackCat;

    @Inject
    public InjectTestObject(BlackCat blackCat){
        this.blackCat = blackCat;
    }

    public BlackCat getBlackCat() {
        return blackCat;
    }



}
