package org.example.core;

import io.undertow.Undertow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebServer {
    private static final Logger logger = LoggerFactory.getLogger(WebServer.class);
    private static final int PORT = System.getProperty("server.port") != null ? Integer.parseInt(System.getProperty("server.port")) : 8906;
    private static final String HOST = System.getProperty("server.host") != null ? System.getProperty("server.host") : "localhost";

    private final RouteManager routeManager = ApplicationInitializer.injector.getInstance(RouteManager.class);
    private Undertow server;

    public WebServer() {
        initializeServer();
    }

    private void initializeServer() {
        this.server = Undertow.builder()
                .addHttpListener(PORT, HOST)
                .setHandler(routeManager.getRouter())
                .build();
    }

    public void startServer() {
        server.start();
        logger.info("[OK] Server started successfully at host:{} and port: {} ✔", HOST, PORT);
    }
}