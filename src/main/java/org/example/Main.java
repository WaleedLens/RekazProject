package org.example;


import org.example.core.ApplicationInitializer;
import org.example.database.MongoDBClient;
import org.example.core.WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);


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

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Application is shutting down, closing database connection...");
            mongoDBClient.close();
        }));

    }
}