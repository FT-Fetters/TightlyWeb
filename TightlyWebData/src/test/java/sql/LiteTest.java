package sql;

import com.heybcat.tightlyweb.common.ioc.IocManager;
import com.heybcat.tightlyweb.sql.entity.Page;
import com.heybcat.tightlyweb.sql.expression.support.DefaultSqlExpressionParser;
import com.heybcat.tightlyweb.sql.support.LiteMapping;
import java.util.HashMap;
import java.util.List;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sql.entity.User;
import sql.mapper.UserMapper;

public class LiteTest {

    private static final Logger log = LoggerFactory.getLogger(LiteTest.class);

    @Test
    public void testMapperSelect(){
        IocManager iocManager = new IocManager("sql");
        LiteMapping liteMapping = LiteMapping.getMapping("F:\\workspace\\tightlyWeb\\test.db", "sql",false ,iocManager);
        UserMapper userMapper = iocManager.getCat(UserMapper.class);
//        List<User> users = userMapper.selectAdultUser();
//        users.forEach(System.out::println);

        User w = userMapper.getByName("小王");
        List<User> users = userMapper.listNameIn(List.of("小王", "小李", "小张"));
        users.forEach(System.out::println);
        System.out.println(w);
    }

    @Test
    public void testSelectPage(){
        IocManager iocManager = new IocManager("sql");
        LiteMapping liteMapping = LiteMapping.getMapping("F:\\workspace\\tightlyWeb\\test.db", "sql",false ,iocManager);
        UserMapper userMapper = iocManager.getCat(UserMapper.class);
        Page<User> userPage = liteMapping.selectPage(10, 2, () -> userMapper.selectAdultUser(1));
        List<User> records = userPage.getRecords();
        log.info("records: {}", records);
    }

    @Test
    public void testSqlExpressionParser(){
        HashMap<String, Object> map = new HashMap<>();
        List<String> names = List.of("小王", "小李","小张");
        map.put("names", names);
        String result = DefaultSqlExpressionParser.parseExpression("(?names) -> { `name` in #{names} }", map);
        System.out.println(result);
    }


}
