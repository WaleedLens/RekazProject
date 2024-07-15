package org.example;

import org.example.core.ApplicationInitializer;
import org.example.database.MongoDBClient;
import org.example.core.WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the main class that starts the application.
 * It initializes the application, loads the routes, starts the server, and handles the shutdown of the application.
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    /**
     * The main method that starts the application.
     * It initializes the application, loads the routes, starts the server, and adds a shutdown hook to close the database connection when the application is shutting down.
     *
     * @param args The command line arguments.
     */
    public static void main(String[] args) {
        logger.info("[ℹ] Initializing Application");
        ApplicationInitializer applicationInitializer = new ApplicationInitializer();
        logger.info("[...] Loading routes...⌛");
        applicationInitializer.initialize();
        logger.info("[OK] Routes loaded successfully ✔");
        logger.info("[...] Starting server...⌛");
        MongoDBClient mongoDBClient = applicationInitializer.getMongoDBClient();

        WebServer webServer = new WebServer();
        webServer.startServer();

        // Add a shutdown hook to close the database connection when the application is shutting down.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Application is shutting down, closing database connection...");
            mongoDBClient.close();
        }));
    }
}