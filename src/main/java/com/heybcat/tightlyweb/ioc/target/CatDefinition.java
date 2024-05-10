package com.heybcat.tightlyweb.ioc.target;

import java.lang.reflect.Constructor;

/**
 * @author Fetters
 */
public class CatDefinition {

    private String name;

    private Class<?> clazz;

    private Constructor<?> targetConstructor;

    public CatDefinition(String name, Class<?> clazz, Constructor<?> targetConstructor) {
        this.name = name;
        this.clazz = clazz;
        this.targetConstructor = targetConstructor;
    }

}
