import com.heybcat.tightlyweb.TightlyWebApplication;
import com.heybcat.tightlyweb.annoation.TightlyWeb;
import java.util.concurrent.locks.LockSupport;
import org.junit.Test;

@TightlyWeb(basePackage = "ioc")
public class TightlyWebApplicationTest {

    @Test
    public void test() {
        TightlyWebApplication.run(TightlyWebApplicationTest.class);
        LockSupport.park();
    }

}
