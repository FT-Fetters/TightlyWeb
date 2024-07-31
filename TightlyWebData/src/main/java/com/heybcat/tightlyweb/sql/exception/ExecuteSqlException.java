package com.heybcat.tightlyweb.sql.exception;

/**
 * @author Fetters
 */
public class ExecuteSqlException extends RuntimeException{

    public ExecuteSqlException(String msg){
        super(msg);
    }

    public ExecuteSqlException(String msg, Throwable cause){
        super(msg, cause);
    }

}
