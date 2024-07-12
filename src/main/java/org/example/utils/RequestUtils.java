package org.example.utils;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

public class RequestUtils {
    public static void sendResponse(HttpServerExchange exchange, String s) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseSender().send(s);
    }
}