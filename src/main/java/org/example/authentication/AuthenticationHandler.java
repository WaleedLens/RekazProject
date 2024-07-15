package org.example.authentication;

import com.google.inject.Inject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

/**
 * This class is responsible for handling the authentication of HTTP requests.
 * It implements the HttpHandler interface and overrides the handleRequest method to provide custom authentication logic.
 */
public class AuthenticationHandler implements HttpHandler {
    private final HttpHandler next;
    private final JwtKeyManager jwtKeyManager;

    /**
     * Constructor for the AuthenticationHandler.
     * Initializes the next HttpHandler and the RsaKeyProvider.
     *
     * @param next           The next HttpHandler in the chain.
     * @param jwtKeyManager The RsaKeyProvider for validating JWT tokens.
     */
    @Inject
    public AuthenticationHandler(HttpHandler next, JwtKeyManager jwtKeyManager) {
        this.next = next;
        this.jwtKeyManager = jwtKeyManager;
    }

    /**
     * Handles the incoming HTTP request.
     * It checks the Authorization header for a Bearer token and validates it.
     * If the token is valid, it passes the request to the next handler in the chain.
     * If the token is invalid or missing, it responds with a 401 Unauthorized status code and an error message.
     *
     * @param exchange The HttpServerExchange object representing the HTTP request/response.
     * @throws Exception If an error occurs while handling the request.
     */
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String authHeader = exchange.getRequestHeaders().getFirst(Headers.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            if (jwtKeyManager.validateJwt(jwt)) {
                next.handleRequest(exchange);
            } else {
                exchange.setStatusCode(StatusCodes.UNAUTHORIZED);
                exchange.getResponseSender().send("Invalid JWT token");
            }
        } else {
            exchange.setStatusCode(StatusCodes.UNAUTHORIZED);
            exchange.getResponseSender().send("Missing Authorization header");
        }
    }
}