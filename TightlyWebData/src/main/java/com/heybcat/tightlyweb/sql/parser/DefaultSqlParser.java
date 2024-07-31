package com.heybcat.tightlyweb.sql.parser;

import com.heybcat.tightlyweb.sql.exception.ExecuteSqlException;
import com.heybcat.tightlyweb.sql.expression.SqlExpressionParser;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Fetters
 */
public class DefaultSqlParser {

    private DefaultSqlParser() {
        throw new UnsupportedOperationException();
    }

    public static String parseSql(String[] expressions, Method method,  String sql, Object... args) {
        if (expressions.length != 0) {
            Parameter[] parameters = method.getParameters();
            if (args.length != parameters.length){
                throw new ExecuteSqlException("args length not equal parameters length");
            }
            Map<String, Object> argMap = new HashMap<>(parameters.length);
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                String name = parameter.getName();
                Object value = args[i];
                argMap.put(name, value);
            }
            sql = SqlExpressionParser.parse(sql, expressions, argMap);
        }
        return sql;
    }

}
