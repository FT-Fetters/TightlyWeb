package com.heybcat.tightlyweb.common.util;

import xyz.ldqc.tightcall.util.StringUtil;

/**
 * @author Fetters
 */
public class UrlUtil {

    private static final String SPLIT_CHAR = "/";

    private UrlUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static String trim(String url) {
        if (StringUtil.isBlank(url)){
            return "";
        }
        url = url.trim();
        url = url.replace("//", SPLIT_CHAR);
        if (url.endsWith(SPLIT_CHAR)) {
            url = url.substring(0, url.length() - 1);
        }
        if (!url.startsWith(SPLIT_CHAR)) {
            url = SPLIT_CHAR + url;
        }
        return url;
    }

}
