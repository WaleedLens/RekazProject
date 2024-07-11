package org.example.core;

import io.undertow.server.HttpHandler;
import org.example.annontations.ApiEndpoint;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

public class ApplicationInitializer {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationInitializer.class);

    public void initialize() {
        Reflections reflections = new Reflections("org.example.controllers", new MethodAnnotationsScanner());
        Set<Method> annotatedMethods = reflections.getMethodsAnnotatedWith(ApiEndpoint.class);
        for (Method method : annotatedMethods) {
            try {
                Object controllerInstance = method.getDeclaringClass().getDeclaredConstructor().newInstance();
                ApiEndpoint apiEndpoint = method.getAnnotation(ApiEndpoint.class);
                RouteManager.getInstance().registerRoute(apiEndpoint.method(), apiEndpoint.path(), (HttpHandler) method.invoke(controllerInstance));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }
}