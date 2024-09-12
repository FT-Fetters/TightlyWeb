package com.heybcat.tightlyweb.sql.support;

import com.heybcat.tightlyweb.common.ioc.IocManager;
import com.heybcat.tightlyweb.sql.DataMapping;

/**
 * @author Feters
 */
public class MappingFactory {

    private MappingFactory() {
    }

    public static DataMapping getMapping(DataDriver driver, String target, String user, String password,
        String basePackage, Boolean dbLog, IocManager iocManager) {
        switch (driver) {
            case SQLITE:
                return LiteMapping.getMapping(target, basePackage, dbLog, iocManager);
            case MYSQL_8:
                return MyMapping.getMapping(target, user, password, basePackage, dbLog, iocManager);
            default:
                return null;
        }
    }

}
