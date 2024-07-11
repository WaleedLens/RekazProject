package org.example.core;

import io.undertow.Undertow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WebServer {
    private final Undertow server;
    private static final Logger logger = LoggerFactory.getLogger(WebServer.class);

    public WebServer(){
        this.server = Undertow.builder().addHttpListener(8906,"localhost")
                .setHandler(RouteManager.getInstance().getRouter())
                .build();

        server.start();
        logger.info("[OK] Server started successfully ✔");


    }



}
