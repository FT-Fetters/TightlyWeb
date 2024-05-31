package com.heybcat.tightlyweb.common.util;

/**
 * @author Fetters
 */
public class TypeUtil {

    private TypeUtil(){
        throw new IllegalStateException("Utility class");
    }

    public static Object parse2TargetType(String value, Class<?> targetType){
        if (targetType.isPrimitive()){
            return TypeUtil.parse2TargetType(value, TypeUtil.getWrapperType(targetType));
        }
        if (targetType.equals(String.class)){
            return value;
        }
        if (targetType.equals(Integer.class)){
            return Integer.valueOf(value);
        }
        if (targetType.equals(Long.class)){
            return Long.valueOf(value);
        }
        if (targetType.equals(Double.class)){
            return Double.valueOf(value);
        }
        if (targetType.equals(Float.class)){
            return Float.valueOf(value);
        }
        if (targetType.equals(Boolean.class)){
            return Boolean.valueOf(value);
        }
        if (targetType.equals(Byte.class)){
            return Byte.valueOf(value);
        }
        return null;
    }

    private static Class<?> getWrapperType(Class<?> primitiveType){
        if (primitiveType.equals(int.class)){
            return Integer.class;
        }
        if (primitiveType.equals(long.class)){
            return Long.class;
        }
        if (primitiveType.equals(double.class)){
            return Double.class;
        }
        if (primitiveType.equals(float.class)){
            return Float.class;
        }
        if (primitiveType.equals(boolean.class)){
            return Boolean.class;
        }
        return null;
    }


}
