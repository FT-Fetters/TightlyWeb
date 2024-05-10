package ioc;

import com.heybcat.tightlyweb.ioc.IocManager;
import ioc.cat.InjectTestObject;
import org.junit.Test;

public class IocTest {

    @Test
    public void testNewIocManager(){
        IocManager iocManager = new IocManager("ioc.cat");
        InjectTestObject injectTestObject = iocManager.getCat(InjectTestObject.class);
        injectTestObject.getBlackCat().eat();
    }

}
