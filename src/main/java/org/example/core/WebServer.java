package org.example.core;

import io.undertow.Undertow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WebServer {
    private final Undertow server;
    private static final Logger logger = LoggerFactory.getLogger(WebServer.class);
    private final int PORT = System.getProperty("server.port") != null ? Integer.parseInt(System.getProperty("server.port")) : 8906;
    private final String HOST = System.getProperty("server.host") != null ? System.getProperty("server.host") : "localhost";

    public WebServer(){
        this.server = Undertow.builder().addHttpListener(PORT,HOST)
                .setHandler(RouteManager.getInstance().getRouter())
                .build();

        server.start();
        logger.info("[OK] Server started successfully at host:{} and port: {} ✔",HOST,PORT);


    }



}
