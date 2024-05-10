package com.heybcat.tightlyweb.server;

import com.heybcat.tightlyweb.common.GlobalEnum;
import com.heybcat.tightlyweb.http.HttpChainGroup;
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

    public WebServer() {
        this(4567);
    }

    public WebServer(int port) {
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

    }

    private void bootHttpServer() {
        log.debug("web server loading");
        this.httpServerApplication = HttpServerApplication.builder()
            .executor(NioServerExec.class)
            .bind(this.port)
            .execNum(Runtime.getRuntime().availableProcessors() * 2)
            .chain(new HttpChainGroup())
            .boot();
    }

}
