package com.heybcat.tightlyweb.sql.util;

/**
 * @author Fetters
 */
public class DBUtil {

    private DBUtil(){
        throw new UnsupportedOperationException();
    }

    public static boolean sameField(String field1, String field2){
        field1 = field1.trim().replace("`", "");
        field2 = field2.trim().replace("`", "");
        return field1.equalsIgnoreCase(field2);
    }

    public static String cleanField(String field){
        return field.trim().replace("`", "").replace("\"", "").toLowerCase();
    }

}
