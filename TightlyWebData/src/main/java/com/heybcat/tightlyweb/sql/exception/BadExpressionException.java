package com.heybcat.tightlyweb.sql.exception;

/**
 * @author Fetters
 */
public class BadExpressionException extends RuntimeException {

    public BadExpressionException(String msg){
        super(msg);
    }

    public  BadExpressionException(String msg, Throwable cause){
        super(msg, cause);
    }

}
