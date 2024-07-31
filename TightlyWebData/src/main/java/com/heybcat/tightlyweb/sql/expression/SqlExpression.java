package com.heybcat.tightlyweb.sql.expression;

import java.util.Map;

/**
 * @author Fetters
 */
public interface SqlExpression {

    String getExpression(Map<String, Object> args);

}
