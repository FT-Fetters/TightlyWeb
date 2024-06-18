package com.heybcat.tightlyweb.common.ioc.target;

import java.lang.reflect.Constructor;
import lombok.Getter;

/**
 * @author Fetters
 */
@Getter
public class CatDefinition {

    private final String name;

    private final Class<?> clazz;

    private final Constructor<?> targetConstructor;

    public CatDefinition(String name, Class<?> clazz, Constructor<?> targetConstructor) {
        this.name = name;
        this.clazz = clazz;
        this.targetConstructor = targetConstructor;
    }

}
