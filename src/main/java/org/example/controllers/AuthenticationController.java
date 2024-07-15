package org.example.controllers;

import com.google.inject.Inject;
import io.undertow.server.HttpHandler;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import org.example.annontations.ApiEndpoint;
import org.example.authentication.JwtKeyManager;
import org.example.model.Token;
import org.example.utils.ParsingUtils;
import org.example.utils.RequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for handling authentication related requests.
 * It provides an endpoint to generate JWT tokens.
 */
public class AuthenticationController {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationController.class);
    private final JwtKeyManager jwtKeyManager;

    /**
     * Constructor for the AuthenticationController.
     * Initializes the rsaKeyProvider with the provided RsaKeyProvider.
     *
     * @param jwtKeyManager The RsaKeyProvider.
     */
    @Inject
    public AuthenticationController(JwtKeyManager jwtKeyManager) {
        this.jwtKeyManager = jwtKeyManager;
    }

    /**
     * Provides an endpoint to generate JWT tokens.
     * The JWT token is generated for the user "user".
     *
     * @return The HttpHandler for the endpoint.
     */
    @ApiEndpoint(method = "GET", path = "/v1/auth/jwt")
    public HttpHandler generateJwt() {
        return httpServerExchange -> {
            try {
                String jwt = jwtKeyManager.generateJwt("user");
                httpServerExchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                httpServerExchange.setStatusCode(StatusCodes.CREATED);

                Token token = new Token("user", jwt);
                String response = ParsingUtils.objectToJson(token);
                RequestUtils.sendResponse(httpServerExchange, response);

            } catch (Exception e) {
                log.error("Error generating JWT", e.getMessage());
                httpServerExchange.setStatusCode(StatusCodes.BAD_REQUEST);
            }
        };
    }

}