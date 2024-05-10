package com.heybcat.tightlyweb.http.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import xyz.ldqc.tightcall.protocol.http.HttpMethodEnum;

/**
 * @author Fetters
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface WebMapping {

    String value();

    HttpMethodEnum method() default HttpMethodEnum.GET;

}
