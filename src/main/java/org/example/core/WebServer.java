package org.example.core;

import io.undertow.Undertow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for managing the web server of the application.
 * It provides methods to initialize and start the server.
 */
public class WebServer {
    private static final Logger logger = LoggerFactory.getLogger(WebServer.class);
    private static final int PORT = System.getProperty("server.port") != null ? Integer.parseInt(System.getProperty("server.port")) : 8906;
    private static final String HOST = System.getProperty("server.host") != null ? System.getProperty("server.host") : "localhost";

    private final RouteManager routeManager = ApplicationInitializer.injector.getInstance(RouteManager.class);
    private Undertow server;

    /**
     * Constructor for the WebServer.
     * Initializes the server.
     */
    public WebServer() {
        initializeServer();
    }

    /**
     * Initializes the server.
     * The server is built with the port and host, and the router from the RouteManager.
     */
    private void initializeServer() {
        this.server = Undertow.builder()
                .addHttpListener(PORT, HOST)
                .setHandler(routeManager.getRouter())
                .build();
    }

    /**
     * Starts the server.
     * Logs a message indicating that the server has started successfully.
     */
    public void startServer() {
        server.start();
        logger.info("[OK] Server started successfully at host:{} and port: {} \uD83D\uDD17", HOST, PORT);
    }
}