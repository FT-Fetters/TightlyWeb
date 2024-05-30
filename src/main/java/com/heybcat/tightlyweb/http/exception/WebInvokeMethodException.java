package com.heybcat.tightlyweb.http.exception;

/**
 * @author Fetters
 */
public class WebInvokeMethodException extends RuntimeException {
    public WebInvokeMethodException(String msg){
        super(msg);
    }

    public WebInvokeMethodException(String msg, Throwable e){
        super(msg, e);
    }

}
