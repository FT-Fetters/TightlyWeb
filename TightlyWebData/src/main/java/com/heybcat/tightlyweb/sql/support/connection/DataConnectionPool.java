package com.heybcat.tightlyweb.sql.support.connection;

import xyz.ldqc.tightcall.util.StringUtil;

/**
 * @author Fetters
 */
public class DataConnectionPool {

    private final String url;

    private final boolean auth;

    private final String user;

    private final String pwd;

    private DataConnectionPool(String url, String user, String pwd) {
        this.url = url;
        if (StringUtil.isNotBlank(user) && StringUtil.isNotBlank(pwd)){
            this.auth = true;
            this.user = user;
            this.pwd = pwd;
        } else {
            this.auth = false;
            this.user = null;
            this.pwd = null;
        }
    }

    public static DataConnectionPool getPool(String url, String user, String password){
        return new DataConnectionPool(url, user, password);
    }

    public static DataConnectionPool getPool(String url){
        return new DataConnectionPool(url, null, null);
    }

}
