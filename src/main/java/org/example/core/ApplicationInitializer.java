package org.example.core;


import com.google.inject.Guice;
import com.google.inject.Injector;
import io.github.cdimascio.dotenv.Dotenv;
import io.undertow.server.HttpHandler;
import org.example.annontations.ApiEndpoint;
import org.example.database.MongoDBClient;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

public class ApplicationInitializer {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationInitializer.class);
    public static Injector injector;

    public void initialize() {

        logger.info("loading environment variables...");
        loadEnvironmentVariables();

        injector = Guice.createInjector(new StorageModule(), new ApplicationModule());

        Reflections reflections = new Reflections("org.example.controllers", new MethodAnnotationsScanner());
        Set<Method> annotatedMethods = reflections.getMethodsAnnotatedWith(ApiEndpoint.class);
        for (Method method : annotatedMethods) {
            try {
                Object controllerInstance = injector.getInstance(method.getDeclaringClass());
                ApiEndpoint apiEndpoint = method.getAnnotation(ApiEndpoint.class);
                RouteManager routeManager = injector.getInstance(RouteManager.class);
                routeManager.registerRoute(apiEndpoint.method(), apiEndpoint.path(), (HttpHandler) method.invoke(controllerInstance));

            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    public MongoDBClient getMongoDBClient() {
        return injector.getInstance(MongoDBClient.class);
    }

    private void loadEnvironmentVariables() {
        Dotenv dotenv = Dotenv.configure().load();
        dotenv.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));


    }


}