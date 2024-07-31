package com.heybcat.tightlyweb.sql.exception;

/**
 * @author Fetters
 */
public class SqlConnectException extends RuntimeException{

    public SqlConnectException(String msg){
        super(msg);
    }

    public SqlConnectException(String msg, Throwable e){
        super(msg, e);
    }

}
