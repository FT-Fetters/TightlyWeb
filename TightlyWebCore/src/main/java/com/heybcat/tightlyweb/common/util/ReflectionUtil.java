package com.heybcat.tightlyweb.common.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author Fetters
 */
public class ReflectionUtil {

    private ReflectionUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static Method getterMethod(Class<?> clazz, String fieldName)
        throws NoSuchMethodException {
        return clazz.getMethod(
            "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1)
        );
    }

    public static Method setterMethod(Class<?> clazz, Field field) throws NoSuchMethodException {
        return clazz.getMethod(
            "set" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1)
            , field.getType());
    }

}
