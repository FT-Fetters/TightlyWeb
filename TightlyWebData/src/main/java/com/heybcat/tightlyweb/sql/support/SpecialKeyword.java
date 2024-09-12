package com.heybcat.tightlyweb.sql.support;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author Fetters
 */
public enum SpecialKeyword {

    /**
     * ID auto increment
     */
    AUTO_INCREMENT("AUTOINCREMENT","AUTO_INCREMENT");

    private final Map<DataDriver, String> keyword;

    SpecialKeyword(String... values){
        DataDriver[] dataDrivers = DataDriver.values();
        keyword = new EnumMap<>(DataDriver.class);
        for (int i = 0; i < dataDrivers.length; i++) {
            keyword.put(dataDrivers[i], values[i]);
        }
    }

    public String getKeyword(DataDriver driver){
        return keyword.get(driver);
    }

}
