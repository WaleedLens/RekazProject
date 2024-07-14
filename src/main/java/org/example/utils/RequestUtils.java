package org.example.utils;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class RequestUtils {
    private static final Logger log = LoggerFactory.getLogger(RequestUtils.class);

    public static void sendResponse(HttpServerExchange exchange, String s) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseSender().send(s);
    }

    public static String headersToString(Map<String, String> headers) {
        return headers.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey().toLowerCase() + ":" + entry.getValue())
                .collect(Collectors.joining("\n"));
    }

    public static String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }

    public static String generateSignedHeaders(Map<String, String> headers) {
        String str = headers.keySet().stream()
                .map(String::toLowerCase)
                .sorted()
                .reduce((header1, header2) -> header1 + ";" + header2)
                .orElse("");
        return str;
    }

}