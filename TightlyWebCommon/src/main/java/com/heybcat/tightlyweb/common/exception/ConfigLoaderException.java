package com.heybcat.tightlyweb.common.exception;


/**
 * @author Fetters
 */
public class ConfigLoaderException extends RuntimeException {

    public ConfigLoaderException(String msg) {
        super(msg);
    }

    public ConfigLoaderException(String msg, Exception e) {
        super(msg, e);
    }

}
