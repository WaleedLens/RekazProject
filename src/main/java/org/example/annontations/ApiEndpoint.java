package org.example.annontations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is a custom annotation used to define API endpoints.
 * It can be applied to methods and it provides two properties: method and path.
 * The 'method' property represents the HTTP method of the endpoint (e.g., GET, POST, etc.).
 * The 'path' property represents the URL path of the endpoint.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ApiEndpoint {
    /**
     * Represents the HTTP method of the endpoint.
     *
     * @return The HTTP method as a string.
     */
    String method();

    /**
     * Represents the URL path of the endpoint.
     *
     * @return The URL path as a string.
     */
    String path();
}