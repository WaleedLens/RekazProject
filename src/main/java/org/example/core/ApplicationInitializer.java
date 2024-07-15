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

/**
 * This class is responsible for initializing the application.
 * It loads environment variables, creates an injector, and registers routes.
 */
public class ApplicationInitializer {
    private static final String ENV_FILE = ".env";
    private static final Logger logger = LoggerFactory.getLogger(ApplicationInitializer.class);
    public static Injector injector;

    /**
     * Initializes the application.
     * Loads environment variables, creates an injector, and registers routes.
     */
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
                logger.info("Registering route: {} {} \uD83D\uDD17", apiEndpoint.method(), apiEndpoint.path());
                RouteManager routeManager = injector.getInstance(RouteManager.class);
                routeManager.registerRoute(apiEndpoint.method(), apiEndpoint.path(), (HttpHandler) method.invoke(controllerInstance));

            } catch (IllegalAccessException | InvocationTargetException e) {
                logger.error("Error registering route: {}", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns an instance of MongoDBClient.
     *
     * @return An instance of MongoDBClient.
     */
    public MongoDBClient getMongoDBClient() {
        return injector.getInstance(MongoDBClient.class);
    }

    /**
     * Loads environment variables from the .env file and sets them as system properties.
     */
    private void loadEnvironmentVariables() {
        Dotenv dotenv = Dotenv.configure().load();
        dotenv.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));
    }
}