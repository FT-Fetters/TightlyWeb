package com.heybcat.tightlyweb;


import com.heybcat.tightlyweb.annoation.TightlyWeb;
import com.heybcat.tightlyweb.common.cache.TightlyCache;
import com.heybcat.tightlyweb.config.ConfigFactory;
import com.heybcat.tightlyweb.config.ConfigManager;
import com.heybcat.tightlyweb.config.TightlyWebConfigEntity;
import com.heybcat.tightlyweb.common.ioc.IocManager;
import com.heybcat.tightlyweb.http.annotation.CrossOrigin;
import com.heybcat.tightlyweb.server.WebServer;
import com.heybcat.tightlyweb.sql.support.LiteMapping;
import java.util.Arrays;
import java.util.stream.Collectors;
import xyz.ldqc.tightcall.util.StringUtil;

/**
 * @author Fetters
 */
public class TightlyWebApplication {

    private final Class<?> bootClass;

    private IocManager iocManager;

    private TightlyWebConfigEntity configEntity;


    private WebServer webServer;

    private TightlyWebApplication(Class<?> bootClass) {
        this.bootClass = bootClass;
        webContext();
    }

    public static TightlyWebApplication run(Class<?> bootClass) {
        return new TightlyWebApplication(bootClass);
    }

    private void webContext() {
        loadConfig();
        loadIoc();
        loadWebServer();
    }

    private void loadConfig() {
        this.configEntity = ConfigFactory.build("application.yml", TightlyWebConfigEntity.class);
        // cross origin config
        CrossOrigin crossOrigin = bootClass.getAnnotation(CrossOrigin.class);
        if (crossOrigin != null && this.configEntity != null){
            this.configEntity.setCrossOriginEnable(true);
            this.configEntity.setCrossOriginAllowOrigin(String.join(",", crossOrigin.allowOrigin()));
        }
    }

    private void loadIoc() {
        TightlyWeb tightlyWeb = bootClass.getAnnotation(TightlyWeb.class);
        String basePackage = tightlyWeb.basePackage();
        this.iocManager = new IocManager(basePackage, bootClass, ioc -> {
            registerSomething(ioc);
            loadLiteMapping(ioc);
            loadCacheModule(ioc);
            loadConfigManager(ioc);
        });
    }

    private void registerSomething(IocManager iocManager) {
        iocManager.register(bootClass, bootClass);
        iocManager.register(TightlyWebConfigEntity.class, configEntity);
    }

    private void loadLiteMapping(IocManager iocManager) {
        LiteMapping liteMapping;
        String db = configEntity.getDbName();
        String mapperPath = configEntity.getDbBasePackage();
        if (StringUtil.isBlank(db)){
            return;
        }
        liteMapping = LiteMapping.getMapping(db, mapperPath, configEntity.getDbLog(), iocManager);
        iocManager.register(LiteMapping.class, liteMapping);
    }

    private void loadCacheModule(IocManager iocManager) {
        iocManager.register(TightlyCache.class, TightlyCache.newCache());
    }

    private void loadConfigManager(IocManager iocManager){
        iocManager.register(ConfigManager.class, new ConfigManager(ConfigFactory.getCacheConfigMap()));
    }

    private void loadWebServer() {
        webServer = new WebServer(iocManager, configEntity);
        webServer.run();
    }

}
