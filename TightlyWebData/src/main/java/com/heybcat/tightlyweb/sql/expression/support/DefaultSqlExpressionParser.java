package com.heybcat.tightlyweb.sql.expression.support;

import com.heybcat.tightlyweb.sql.exception.BadExpressionException;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

/**
 * @author Fetters
 */
public class DefaultSqlExpressionParser {

    private static final String CONDITION_START = "(";

    private static final String CONDITION_END = ")";

    private DefaultSqlExpressionParser() {
    }


    public static String parseExpression(String expression, Map<String, Object> args) {
        String rightPart = expressionAvailable(expression, args);
        if (rightPart.isEmpty()){
            return "";
        }
        return parseTargetExpression(rightPart, args);
    }

    private static String expressionAvailable(String expression, Map<String, Object> args) {
        String[] split = expression.split("->");
        if (split.length != 2) {
            throw new BadExpressionException("parse exception '"+ expression +"' fail, cause bad wrong format, "
                + "may miss '->'");
        }

        String condition = split[0].trim();
        String targetExpression = split[1];

        if (!condition.startsWith(CONDITION_START) || !condition.endsWith(CONDITION_END)) {
            return "";
        }
        condition = condition.substring(1, condition.length() - 1);
        String[] conditionFields = condition.split(",");
        boolean available = true;
        for (String conditionField : conditionFields) {
            available = available && conditionJudge(conditionField, args);
        }
        if (!available) {
            return "";
        }
        return targetExpression;

    }

    private static boolean conditionJudge(String conditionField, Map<String, Object> args) {
        conditionField = conditionField.trim();
        String conditionFieldName = getConditionFieldName(conditionField);
        Object value = args.get(conditionFieldName);
        if (conditionField.startsWith("?") && isEmpty(value)) {
            return false;
        }

        if (conditionField.contains("<") && conditionField.contains(">")) {
            if (!isStringOrInteger(value)) {
                return false;
            }
            String targetValue = conditionField.substring(conditionField.indexOf("<"), conditionField.indexOf(">"))
                .trim();
            if (targetValue.matches("[-+]?\\d*\\.?\\d+")) {
                if (targetValue.contains(".")) {
                    return Double.parseDouble(targetValue) > Double.parseDouble(String.valueOf(value));
                } else {
                    return Integer.parseInt(targetValue) > Integer.parseInt(String.valueOf(value));
                }
            } else {
                return targetValue.compareTo(String.valueOf(value)) == 0;
            }

        }

        return true;

    }

    private static boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof CharSequence) {
            return ((CharSequence) value).length() == 0;
        }
        if (Collection.class.isAssignableFrom(value.getClass())) {
            return ((Collection<?>) value).isEmpty();
        }
        if (value.getClass().isArray()) {
            return ((Object[]) value).length == 0;
        }
        return true;
    }

    private static String getConditionFieldName(String conditionField) {
        String fieldName = String.valueOf(conditionField);
        if (conditionField.startsWith("?")) {
            fieldName = fieldName.substring(1);
        }
        if (conditionField.contains("<") && conditionField.contains(">")) {
            fieldName = fieldName.substring(0, fieldName.indexOf("<"));
        }
        return fieldName;
    }

    private static boolean isStringOrInteger(Object value) {
        return value instanceof String || value instanceof Number;
    }

    private static String parseTargetExpression(String targetExpression, Map<String, Object> args) {
        String resultTargetExpression = targetExpression.trim();
        if (!resultTargetExpression.startsWith("{") || !resultTargetExpression.endsWith("}")) {
            throw new BadExpressionException("parse exception fail, missing { and }");
        }
        resultTargetExpression = resultTargetExpression.substring(1, resultTargetExpression.length() - 1);
        int scanIndex = 0;
        while (resultTargetExpression.indexOf("#{", scanIndex) != -1) {
            int startIndex = resultTargetExpression.indexOf("#{", scanIndex);
            int endIndex = resultTargetExpression.indexOf("}", startIndex);
            String field = resultTargetExpression.substring(startIndex + 2, endIndex);
            Object o = args.get(field);
            if (o == null) {
                throw new BadExpressionException("parse exception fail, missing field " + field);
            }

            resultTargetExpression = resultTargetExpression.replace(resultTargetExpression.substring(startIndex, endIndex + 1),
                parseObject2Target(o));
        }
        return afterHandle(resultTargetExpression);
    }

    private static String parseObject2Target(Object o) {
        if (o instanceof CharSequence) {
            return "'" + o + "'";
        }
        if (o instanceof Number) {
            return String.valueOf(o);
        }
        if (o instanceof Collection) {
            StringBuilder sb = new StringBuilder();
            Collection<?> collection = (Collection<?>) o;
            for (Object object : collection) {
                sb.append(parseObject2Target(object)).append(",");
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            sb.insert(0, "(").append(")");
            return sb.toString();
        }
        if (o.getClass().isArray()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < Array.getLength(o); i++) {
                sb.append(parseObject2Target(Array.get(o, i))).append(",");
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            sb.insert(0, "(").append(")");
            return sb.toString();
        }
        return "";
    }

    private static String afterHandle(String resultTargetExpression){
        resultTargetExpression = resultTargetExpression.trim();
        resultTargetExpression = resultTargetExpression.replace("%'", "'%");
        resultTargetExpression = resultTargetExpression.replace("'%", "%'");
        return resultTargetExpression;
    }

}
