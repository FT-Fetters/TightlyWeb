package util;


import com.heybcat.tightlyweb.common.resource.DefaultResourceLoader;
import com.heybcat.tightlyweb.common.resource.Resource;
import com.heybcat.tightlyweb.common.util.YamlUtil;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;
import org.junit.Test;

public class YamlTest {

    @Test
    public void testYamlUtil() throws FileNotFoundException {
        Map<String, String> map = YamlUtil.loadConfigMap(
            new Resource("F:\\workspace\\tightlyWeb\\application.yml",
                new FileInputStream("F:\\workspace\\tightlyWeb\\application.yml")));
        System.out.println(map.get("server.port"));
        System.out.println(map.get("test.one"));
        System.out.println(map.get("env.os"));
    }

}
