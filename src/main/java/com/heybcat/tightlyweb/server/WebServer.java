package com.heybcat.tightlyweb.server;

import com.heybcat.tightlyweb.common.GlobalEnum;
import com.heybcat.tightlyweb.http.HttpChainGroup;
import com.heybcat.tightlyweb.http.core.WebDispatcher;
import com.heybcat.tightlyweb.ioc.IocManager;
import lombok.extern.slf4j.Slf4j;
import xyz.ldqc.tightcall.server.HttpServerApplication;
import xyz.ldqc.tightcall.server.exec.support.NioServerExec;

/**
 * @author Fetters
 */
@Slf4j
public class WebServer {

    private final int port;

    private HttpServerApplication httpServerApplication;

    private WebDispatcher webDispatcher;

    private final IocManager iocManager;

    public WebServer(IocManager iocManager) {
        this(iocManager, 4567);
    }

    public WebServer(IocManager iocManager, int port) {
        this.iocManager = iocManager;
        this.port = port;
        if (port < 0 || port > GlobalEnum.MAX_PORT) {
            throw new IllegalArgumentException("port must be between 0 and 65535");
        }
    }

    public void run() {
        instantiateWebDispatcher();
        bootHttpServer();
    }

    private void instantiateWebDispatcher(){
        this.webDispatcher = new WebDispatcher(iocManager);
    }

    private void bootHttpServer() {
        log.debug("web server loading");
        this.httpServerApplication = HttpServerApplication.builder()
            .executor(NioServerExec.class)
            .bind(this.port)
            .execNum(Runtime.getRuntime().availableProcessors() * 2)
            .chain(new HttpChainGroup(webDispatcher))
            .boot();
    }

}
