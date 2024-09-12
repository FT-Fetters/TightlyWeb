package com.heybcat.tightlyweb.sql.support;

import com.heybcat.tightlyweb.common.ioc.IocManager;
import java.util.List;

/**
 * @author Fetters
 */
public class MyMapping extends BaseMapping {


    private MyMapping(String driverClass, String type, String target, String user, String password, String basePackage,
        Boolean dbLog, IocManager iocManager) {
        super(driverClass, type, target,user, password, basePackage, dbLog, iocManager);
    }

    public static MyMapping getMapping(String target, String user, String password, String basePackage, Boolean dbLog,
        IocManager iocManager) {
        if (!target.startsWith("//")){
            target = "//" + target;
        }
        return new MyMapping(DataDriver.MYSQL_8.getDriverClass(), DataDriver.MYSQL_8.getType(), target, user, password,
            basePackage, dbLog, iocManager);
    }

    @Override
    public boolean tableExist(String tableName) {
        String checkTableExistSql = "SELECT table_name FROM information_schema.TABLES WHERE table_name = '" + tableName + "'";
        List<String> results = execute(String.class, checkTableExistSql, true);
        return !results.isEmpty();
    }

    @Override
    public String getTableDesc(String tableName) {
        List<String> results = execute(String.class, "show create table `" + tableName + "`", true);
        if (!results.isEmpty()) {
            return results.get(0);
        }
        return "";
    }
}
