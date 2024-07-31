package com.heybcat.tightlyweb.common.ioc;

import com.heybcat.tightlyweb.common.ioc.annotation.Cat;
import com.heybcat.tightlyweb.common.ioc.annotation.Inject;
import com.heybcat.tightlyweb.common.ioc.invocation.BeforeInvocation;
import com.heybcat.tightlyweb.common.ioc.proxy.CatProxyMethodInterceptor;
import com.heybcat.tightlyweb.common.ioc.proxy.ProxyClassFactory;
import com.heybcat.tightlyweb.common.ioc.target.CatDefinition;
import com.heybcat.tightlyweb.common.util.ReflectionUtil;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import xyz.ldqc.tightcall.consumer.proxy.factory.ProxySupport;
import xyz.ldqc.tightcall.util.PackageUtil;
import xyz.ldqc.tightcall.util.StringUtil;

/**
 * scan and get instance which annotation with cat
 *
 * @author Fetters
 */
@Slf4j
public class IocManager {

    private final String basePackage;

    private final Map<String, Object> nameCatMap;

    private final Map<Class<?>, Object> typeCatMap;

    private List<CatDefinition> catDefinitions;

    private final Class<?> bootClass;

    public IocManager(final String basePackage) {
        this(basePackage, IocManager.class);
    }

    public IocManager(final String basePackage, Class<?> bootClass){
        this(basePackage, bootClass, null);
    }

    public IocManager(final String basePackage, Class<?> bootClass, BeforeInvocation beforeInvocation){
        this.basePackage = basePackage;
        this.nameCatMap = new ConcurrentHashMap<>();
        this.typeCatMap = new ConcurrentHashMap<>();
        this.bootClass = bootClass;
        if (beforeInvocation != null){
            beforeInvocation.invoke(this);
        }
        create();
    }



    private void create() {
        scanCatDefinition();
        getCat();
    }

    private void scanCatDefinition() {
        // scan target package all class
        List<Class<?>> packageClasses = PackageUtil.getPackageClasses(basePackage, bootClass);
        catDefinitions = new ArrayList<>();
        for (Class<?> clazz : packageClasses) {
            // find all annotation with cat and save as catDefinition
            if (isAnnotationWithCat(clazz)) {
                Cat cat = clazz.getAnnotation(Cat.class);
                String value = cat.value();
                Constructor<?> targetConstructor = getTargetConstructor(clazz);
                catDefinitions.add(new CatDefinition(value, clazz, targetConstructor));
            }
        }
    }

    private boolean isAnnotationWithCat(Class<?> clazz) {
        return clazz.isAnnotationPresent(Cat.class);
    }

    private Constructor<?> getTargetConstructor(Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        Constructor<?> noArgConstructor = null;
        for (Constructor<?> constructor : constructors) {
            // record no arg constructor
            if (constructor.getParameterCount() == 0) {
                noArgConstructor = constructor;
            }
            // only annotation with inject is target
            if (constructor.isAnnotationPresent(Inject.class)) {
                return constructor;
            }
        }
        // if not find target, use no arg constructor
        if (noArgConstructor == null){
            log.warn("cat {} can not find target constructor, may not use @Inject?", clazz.getName());
        }
        return noArgConstructor;
    }

    private void getCat() {
        if (catDefinitions == null || catDefinitions.isEmpty()) {
            return;
        }
        for (CatDefinition catDefinition : catDefinitions) {
            getCat(catDefinition);
        }

    }

    @SuppressWarnings("unchecked")
    public <T> T getCat(Class<T> clazz) {
        return (T) getCat(new CatDefinition(null, clazz, getTargetConstructor(clazz)));
    }

    private Object getCat(CatDefinition catDefinition) {
        Object cat = getCatFormExist(catDefinition);
        if (cat != null) {
            return cat;
        }
        if (!ReflectionUtil.hasAnnotation(catDefinition.getClazz(), Cat.class)) {
            return null;
        }
        return newCatInstance(catDefinition);
    }

    private Object newCatInstance(CatDefinition catDefinition) {
        // if map not exist cat, proxy target class first
        ProxySupport proxySupport = new ProxySupport();
        proxySupport.setTargetClass(catDefinition.getClazz());
        proxySupport.setInterceptor(new CatProxyMethodInterceptor());
        Class<?> proxyClass = ProxyClassFactory.doByteBuddyProxy(proxySupport);
        // get target constructor parameters
        Constructor<?> targetConstructor = catDefinition.getTargetConstructor();
        if (targetConstructor == null) {
            log.error("cat {} can not find target constructor, may not use @Inject?", catDefinition.getClazz().getName());
            return null;
        }
        Class<?>[] parameterTypes = targetConstructor.getParameterTypes();
        if (parameterTypes.length == 0) {
            // parameter length is 0 means no arg constructor or inject constructor is no arg constructor
            try {
                Object catInstance = proxyClass.getConstructor().newInstance();
                putCatInMap(catDefinition, catInstance);
                return catInstance;
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                log.error("get cat instance fail, maybe is can not get constructor or instance exception", e);
                return null;
            }
        }
        List<Object> parameters = new ArrayList<>();
        // get all need parameters by getCat method
        for (Class<?> parameterType : parameterTypes) {
            Object parameter = getCat(parameterType);
            parameters.add(parameter);
        }
        try {
            Object catInstance = proxyClass.getConstructor(parameterTypes).newInstance(parameters.toArray());
            putCatInMap(catDefinition, catInstance);
            return catInstance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            log.error("get cat instance fail, maybe is can not get constructor or instance exception", e);
            return null;
        }
    }

    private void putCatInMap(CatDefinition catDefinition, Object catInstance) {
        typeCatMap.put(catDefinition.getClazz(), catInstance);
        nameCatMap.put(
            StringUtil.isNotBlank(
                catDefinition.getName()) ? catDefinition.getName() : catDefinition.getClazz().getName(),
            catInstance);
    }

    private Object getCatFormExist(CatDefinition catDefinition) {
        String name = catDefinition.getName();
        Object cat = null;
        if (StringUtil.isNotBlank(name)) {
            cat = nameCatMap.get(name);
        }
        if (cat == null) {
            cat = typeCatMap.get(catDefinition.getClazz());
        }
        return cat;
    }

    public List<Object> listAnnotationWith(Class<? extends Annotation> annotation){
        List<Object> cats = new ArrayList<>();
        for (CatDefinition catDefinition : catDefinitions) {
            if (catDefinition.getClazz().isAnnotationPresent(annotation)) {
                cats.add(getCat(catDefinition));
            }
        }
        return cats;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> listInterfaceWith(Class<T> clazz){
        if (!clazz.isInterface()){
            return Collections.emptyList();
        }
        List<T> cats = new ArrayList<>();
        for (CatDefinition catDefinition : catDefinitions) {
            Class<?>[] interfaces = catDefinition.getClazz().getInterfaces();
            for (Class<?> interfaceClass : interfaces) {
                if (interfaceClass.equals(clazz)) {
                    cats.add((T) getCat(catDefinition));
                }
            }
        }
        return cats;
    }

    public void register(String name, Object cat) {
        nameCatMap.put(name, cat);
        if (ReflectionUtil.isCglibProxyClass(cat.getClass()) || ReflectionUtil.isByteBuddyProxyClass(cat.getClass())) {
            if (cat.getClass().getSuperclass() != Object.class) {
                typeCatMap.put(cat.getClass().getSuperclass(), cat);
            }else {
                typeCatMap.put(cat.getClass().getInterfaces()[0], cat);
            }
            return;
        }
        typeCatMap.put(cat.getClass(), cat);
    }

    public void register(Class<?> clazz, Object cat) {
        register(clazz.getName(), cat);
    }

    public Class<?> getBootClass() {
        return bootClass;
    }


}
