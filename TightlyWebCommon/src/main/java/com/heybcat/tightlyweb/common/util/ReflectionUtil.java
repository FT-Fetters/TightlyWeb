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

    /**
     * Get the set method of the field according to the field
     * @param clazz the class
     * @param fieldName target field name
     * @return setter method
     * @throws NoSuchMethodException exception
     */
    public static Method getterMethod(Class<?> clazz, String fieldName)
        throws NoSuchMethodException {
        return clazz.getMethod(
            "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1)
        );
    }

    /**
     * Get the set method of the field according to the field
     * @param clazz the class
     * @param field target field
     * @return setter method
     * @throws NoSuchMethodException exception
     */
    public static Method setterMethod(Class<?> clazz, Field field) throws NoSuchMethodException {
        return clazz.getMethod(
            "set" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1)
            , field.getType());
    }


    public static boolean isProxy(Object object) {
        Class<?> clazz = object.getClass();
        return isCglibProxyClass(clazz) || isByteBuddyProxyClass(clazz);
    }

    public static boolean isCglibProxyClass(Class<?> clazz) {
        return clazz != null && clazz.getName().contains("$$") && clazz.getSuperclass() != null;
    }

    public static boolean isByteBuddyProxyClass(Class<?> clazz) {
        return clazz != null && clazz.getName().contains("ByteBuddy");
    }


}
