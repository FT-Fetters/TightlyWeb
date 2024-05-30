package com.heybcat.tightlyweb.config;

import com.heybcat.tightlyweb.annoation.ConfigValue;
import com.heybcat.tightlyweb.common.resource.DefaultResourceLoader;
import com.heybcat.tightlyweb.common.resource.Resource;
import com.heybcat.tightlyweb.common.util.ReflectionUtil;
import com.heybcat.tightlyweb.common.util.YamlUtil;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Fetters
 */
public class ConfigFactory {

    private static final Logger log = LoggerFactory.getLogger(ConfigFactory.class);

    private ConfigFactory() {
        throw new IllegalStateException("Illegal operation");
    }

    public static <T> T build(String config, Class<T> configClass) {

        // load config resource and read config map
        DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.load(config);
        Map<String, String> configMap = YamlUtil.loadConfigMap(resource);

        Field[] declaredFields = configClass.getDeclaredFields();
        T configEntity;
        try {
            // get config entity instance
            configEntity = configClass.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            log.error("instance config entity fail", e);
            return null;
        }
        for (Field declaredField : declaredFields) {
            // step every field, check have 'ConfigValue' annotation
            ConfigValue configValue = declaredField.getAnnotation(ConfigValue.class);
            if (configValue == null || configMap.get(configValue.key()) == null) {
                continue;
            }
            String value = configMap.get(configValue.key());
            try {
                // get setter method
                Method settermethod = ReflectionUtil.setterMethod(configClass, declaredField);
                // set field value where get by yaml file
                setValue(value, configEntity, settermethod, declaredField.getType());
            } catch (NoSuchMethodException e) {
                log.error("can not found {} field get method", declaredField.getName(), e);
            } catch (InvocationTargetException | IllegalAccessException e) {
                log.error("can not set {} field value", declaredField.getName(), e);
            }

        }
        return configEntity;
    }

    private static void setValue(String value, Object target, Method setterMethod,
        Class<?> fieldType)
        throws InvocationTargetException, IllegalAccessException {
        if (fieldType.isPrimitive()){
            throw new IllegalAccessException("config field cannot be primitive");
        }
        if (fieldType.equals(String.class)) {
            setterMethod.invoke(target, value);
            return;
        }
        if (fieldType.equals(Integer.class)) {
            setterMethod.invoke(target, Integer.valueOf(value));
            return;
        }
        if (fieldType.equals(Long.class)) {
            setterMethod.invoke(target, Long.valueOf(value));
            return;
        }
        if (fieldType.equals(Boolean.class)) {
            setterMethod.invoke(target, Boolean.valueOf(value));
            return;
        }
        if (fieldType.equals(Double.class)) {
            setterMethod.invoke(target, Double.valueOf(value));
            return;
        }
        if (fieldType.equals(Float.class)) {
            setterMethod.invoke(target, Float.valueOf(value));
            return;
        }
        if (fieldType.equals(Byte.class)) {
            setterMethod.invoke(target, Byte.valueOf(value));
            return;
        }
        if (fieldType.equals(Short.class)) {
            setterMethod.invoke(target, Short.valueOf(value));
            return;
        }
        if (fieldType.equals(Character.class)) {
            setterMethod.invoke(target, value.charAt(0));
        }
    }


}
