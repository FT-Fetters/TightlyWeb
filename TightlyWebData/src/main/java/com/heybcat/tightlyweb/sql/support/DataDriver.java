package com.heybcat.tightlyweb.sql.support;

import com.heybcat.tightlyweb.sql.DataMapping;
import lombok.Getter;

/**
 * @author Fetters
 */

@Getter
public enum DataDriver {
    /**
     * sqlite
     */
    SQLITE("org.sqlite.JDBC", "sqlite", LiteMapping.class),
    /**
     * mysql version 8
     */
    MYSQL_8("com.mysql.cj.jdbc.Driver", "mysql", MyMapping.class);

    private final String driverClass;

    private final String type;

    private final Class<? extends DataMapping> dataMappingClass;

    DataDriver(String driverClass, String type, Class<? extends DataMapping> dataMappingClass){
        this.driverClass = driverClass;
        this.type = type;
        this.dataMappingClass = dataMappingClass;
    }

    public static DataDriver getDriver(String type){
        for(DataDriver driver : DataDriver.values()){
            if(driver.type.equals(type)){
                return driver;
            }
        }
        throw new IllegalArgumentException("Unknown driver type");
    }

}
