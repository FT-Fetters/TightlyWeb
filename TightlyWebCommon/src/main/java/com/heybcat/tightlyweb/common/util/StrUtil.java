package com.heybcat.tightlyweb.common.util;

/**
 * @author Fetters
 */
public class StrUtil {

    public static final char SPACE = ' ';

    private StrUtil(){}

    public static String format(final String format, final Object... args) {
        String formatStr = format;
        for (int i = 0; i < args.length; i++) {
            formatStr = formatStr.replaceFirst("\\{"+ i + "}", args[i].toString());
        }
        return formatStr;
    }

    public static int countLeadingSpaces(String str) {
        int count = 0;
        while (count < str.length() && str.charAt(count) == SPACE) {
            count++;
        }
        return count;
    }

    public static String camelCase2Underline(String camelCase){
        return camelCase.replaceAll("([A-Z])", "_$1").toLowerCase();
    }

}
