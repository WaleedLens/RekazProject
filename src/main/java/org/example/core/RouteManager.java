package org.example.core;

import com.google.inject.Inject;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import org.example.authentication.AuthenticationHandler;
import org.example.authentication.JwtKeyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for managing the routes of the application.
 * It provides methods to register routes and get the router.
 */
public class RouteManager {
    private static final Logger logger = LoggerFactory.getLogger(RouteManager.class);
    private final RoutingHandler router = Handlers.routing();
    private final JwtKeyManager jwtKeyManager;

    /**
     * Constructor for the RouteManager.
     * Initializes the rsaKeyProvider with the provided RsaKeyProvider.
     *
     * @param jwtKeyManager The RsaKeyProvider.
     */
    @Inject
    public RouteManager(JwtKeyManager jwtKeyManager) {
        this.jwtKeyManager = jwtKeyManager;
    }

    /**
     * Registers a route with the provided method, path, and handler.
     * If the path does not contain "auth", an AuthenticationHandler is added to the handler.
     *
     * @param method  The HTTP method of the route.
     * @param path    The path of the route.
     * @param handler The handler of the route.
     */
    public void registerRoute(String method, String path, HttpHandler handler) {
        if (!path.contains("auth")) {
            handler = new AuthenticationHandler(handler, jwtKeyManager);
        }
        router.add(method, path, handler);
        logger.info("New route added: {} {}", method, path);
    }

    /**
     * Returns the router.
     *
     * @return The router.
     */
    public RoutingHandler getRouter() {
        return router;
    }
}