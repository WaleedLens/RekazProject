package org.example.core;

import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouteManager {
    private static  final RouteManager INSTANCE = new RouteManager();
    private static final Logger logger = LoggerFactory.getLogger(RouteManager.class);
    private final RoutingHandler router = Handlers.routing();

    private RouteManager() {}

    public static RouteManager getInstance() {
        return INSTANCE;
    }

    public void registerRoute(String method, String path, HttpHandler handler) {
        router.add(method, path, handler);
        logger.info("New Route added ");
    }

    public RoutingHandler getRouter() {
        return router;
    }


}
