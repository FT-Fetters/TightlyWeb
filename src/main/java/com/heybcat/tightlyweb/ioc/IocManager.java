package com.heybcat.tightlyweb.ioc;

import com.heybcat.tightlyweb.ioc.annotation.Cat;
import com.heybcat.tightlyweb.ioc.annotation.Inject;
import com.heybcat.tightlyweb.ioc.target.CatDefinition;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import xyz.ldqc.tightcall.util.PackageUtil;

/**
 * @author Fetters
 */
public class IocManager {

    private final String basePackage;

    private Map<String, Object> nameCatMap;

    private Map<Class<?>, Object> typeCatMap;

    private List<CatDefinition> catDefinitions;

    public IocManager(final String basePackage){
        this.basePackage = basePackage;
        nameCatMap = new ConcurrentHashMap<>();
        typeCatMap = new ConcurrentHashMap<>();
        create();
    }

    private void create(){
        scanCatDefinition();
    }

    private void scanCatDefinition(){
        List<Class<?>> packageClasses = PackageUtil.getPackageClasses(basePackage,
            IocManager.class);
        for (Class<?> clazz : packageClasses) {
            if (isAnnotationWithCat(clazz)){
                Cat cat = clazz.getAnnotation(Cat.class);
                String value = cat.value();
                Constructor<?> targetConstructor = getTargetConstructor(clazz);
                catDefinitions.add(new CatDefinition(value, clazz, targetConstructor));
            }
        }
    }

    private boolean isAnnotationWithCat(Class<?> clazz){
        return clazz.isAnnotationPresent(Cat.class);
    }

    private Constructor<?> getTargetConstructor(Class<?> clazz){
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            if (constructor.isAnnotationPresent(Inject.class)) {
                return constructor;
            }
        }
        return null;
    }




}
