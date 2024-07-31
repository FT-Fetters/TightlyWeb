package com.heybcat.tightlyweb.sql;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Function;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * @author Fetters
 */
@FunctionalInterface
public interface MapperMethodRef<T> extends Serializable {

    /**
     * when invoke this method will return target query result
     *
     * @return mapper query result
     */
    List<T> query();


    /**
     * get SerializedLambda by writeReplace
     * @return SerializedLambda
     * @throws NoSuchMethodException NoSuchMethodException
     * @throws InvocationTargetException InvocationTargetException
     * @throws IllegalAccessException IllegalAccessException
     */
    default SerializedLambda getSerializedLambda()
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = getClass().getDeclaredMethod("writeReplace");
        ReflectionUtils.makeAccessible(method);
        SerializedLambda serializedLambda = (SerializedLambda) method.invoke(this);
        ReflectionUtils.makeAccessible(method);
        return serializedLambda;
    }

    /**
     * get method that invoke by this mapper method
     * @return Method
     * @throws NoSuchMethodException NoSuchMethodException
     * @throws IllegalAccessException IllegalAccessException
     * @throws InvocationTargetException InvocationTargetException
     * @throws ClassNotFoundException ClassNotFoundException
     */
    default Method getMethod()
        throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        SerializedLambda serializedLambda = getSerializedLambda();
        String methodName = serializedLambda.getImplMethodName();
        String className = serializedLambda.getImplClass();
        Class<?> clazz = Class.forName(className.replace("/", "."));
        return clazz.getDeclaredMethod(methodName);
    }
}
