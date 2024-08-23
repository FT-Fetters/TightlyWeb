package com.heybcat.tightlyweb.common.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.platform.commons.util.ReflectionUtils;

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


    public static boolean hasAnnotation(Class<?> clazz, Class<? extends Annotation> annotation) {
        // 获取类上的所有注解
        Annotation[] annotations = clazz.getAnnotations();

        // 检查类上的每个注解是否包含目标注解作为元注解
        for (Annotation ann : annotations) {
            if (ann.annotationType().equals(annotation)) {
                return true;
            }
            Annotation[] metaAnnotations = ann.annotationType().getAnnotations();
            if (Arrays.stream(metaAnnotations).anyMatch(annotation::isInstance)) {
                return true;
            }
        }

        return false;
    }

    public static Annotation getAnnotation(Class<?> clazz, Class<? extends Annotation> annotation) {
        // 获取类上的所有注解
        Annotation[] annotations = clazz.getAnnotations();

        // 检查类上的每个注解是否包含目标注解作为元注解
        for (Annotation ann : annotations) {
            if (ann.annotationType().equals(annotation)) {
                return ann;
            }
            Annotation[] metaAnnotations = ann.annotationType().getAnnotations();
            if (Arrays.stream(metaAnnotations).anyMatch(annotation::isInstance)) {
                for (Annotation metaAnnotation : metaAnnotations) {
                    if (metaAnnotation.annotationType().equals(annotation)) {
                        return metaAnnotation;
                    }
                }
            }
        }
        return null;
    }



    public static boolean setFieldValue(Object object, String fieldName, Object value) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            ReflectionUtils.makeAccessible(field);
            field.set(object, value);
            return true;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return false;
        }
    }

    public static Object getFieldValue(Object object, String fieldName) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            ReflectionUtils.makeAccessible(field);
            return field.get(object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }




}
