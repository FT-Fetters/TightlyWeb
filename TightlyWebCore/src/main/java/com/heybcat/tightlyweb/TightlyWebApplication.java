package com.heybcat.tightlyweb;


import com.heybcat.tightlyweb.annoation.TightlyWeb;
import com.heybcat.tightlyweb.common.cache.TightlyCache;
import com.heybcat.tightlyweb.config.ConfigFactory;
import com.heybcat.tightlyweb.config.ConfigManager;
import com.heybcat.tightlyweb.config.TightlyWebConfigEntity;
import com.heybcat.tightlyweb.common.ioc.IocManager;
import com.heybcat.tightlyweb.http.annotation.CrossOrigin;
import com.heybcat.tightlyweb.server.WebServer;
import com.heybcat.tightlyweb.sql.DataMapping;
import com.heybcat.tightlyweb.sql.support.DataDriver;
import com.heybcat.tightlyweb.sql.support.MappingFactory;
import lombok.Getter;
import xyz.ldqc.tightcall.util.StringUtil;

/**
 * @author Fetters
 */
public class TightlyWebApplication {

    private final Class<?> bootClass;

    @Getter
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
            loadDataMapping(ioc);
            loadCacheModule(ioc);
            loadConfigManager(ioc);
        });
    }

    private void registerSomething(IocManager iocManager) {
        iocManager.register(bootClass, bootClass);
        iocManager.register(TightlyWebConfigEntity.class, configEntity);
    }

    private void loadDataMapping(IocManager iocManager) {
        DataMapping dataMapping;
        String target = configEntity.getDbTarget();
        String mapperPath = configEntity.getDbBasePackage();
        String user = configEntity.getDbUser();
        String password = configEntity.getDbPassword();
        String type = configEntity.getDbType();
        if (StringUtil.isBlank(target)){
            return;
        }
        dataMapping = MappingFactory.getMapping(DataDriver.getDriver(type), target, user, password, mapperPath, configEntity.getDbLog(), iocManager);
        iocManager.register(DataMapping.class, dataMapping);
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
