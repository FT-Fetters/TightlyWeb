package com.heybcat.tightlyweb;


import com.heybcat.tightlyweb.annoation.TightlyWeb;
import com.heybcat.tightlyweb.config.ConfigFactory;
import com.heybcat.tightlyweb.config.TightlyWebConfigEntity;
import com.heybcat.tightlyweb.common.ioc.IocManager;
import com.heybcat.tightlyweb.server.WebServer;

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
    }

    private void loadIoc() {
        TightlyWeb tightlyWeb = bootClass.getAnnotation(TightlyWeb.class);
        String basePackage = tightlyWeb.basePackage();
        this.iocManager = new IocManager(basePackage);
    }

    private void loadWebServer() {
        webServer = new WebServer(iocManager, configEntity.getPort());
        webServer.run();
    }

}
