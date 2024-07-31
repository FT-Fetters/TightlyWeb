package com.heybcat.tightlyweb.sql.expression;

import static com.heybcat.tightlyweb.sql.expression.support.DefaultSqlExpressionParser.parseExpression;

import java.util.Map;

/**
 * @author Fetters
 */
public class SqlExpressionParser {

    private SqlExpressionParser(){
        throw new UnsupportedOperationException();
    }

    public static String parse(String sql, String[] expressions, Map<String, Object> args){
        for (int i = 0; i < expressions.length; i++) {
            String expression = expressions[i];
            sql = sql.replace("$" + i + "$", parseExpression(expression, args));
        }
        sql = handleWhere(sql);
        return sql;
    }

    private static String handleWhere(String sql){
        String upperCase = sql.toUpperCase();
        if (!upperCase.contains("?WHERE")) {
            return sql;
        }
        int whereStart = upperCase.indexOf("?WHERE");
        upperCase = upperCase.substring(whereStart);
        String[] flags = {"=", "IN", "LIKE", ">" ,"<", "NOT"};
        for (String flag : flags) {
            if (upperCase.contains(flag)) {
                return sql.replace(sql.substring(whereStart, whereStart + 6), "WHERE");
            }
        }
        return sql.replace(sql.substring(whereStart, whereStart + 6), "");
    }

}
