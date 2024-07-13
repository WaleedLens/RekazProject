package org.example.core;

import com.google.inject.Singleton;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.example.model.Blob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MongoDBClient {
    private static final Logger logger = LoggerFactory.getLogger(MongoClient.class);

    private final MongoClient mongoClient;
    private final MongoDatabase database;

    public MongoDBClient() {
        logger.info("Initializing MongoDBClient...");
        String connectionString = buildConnectionString();
        this.mongoClient = MongoClients.create(connectionString);
        String dbName = System.getProperty("DB_NAME");
        this.database = mongoClient.getDatabase(dbName);
        logger.info("MongoDBClient initialized successfully.");
    }

    private String buildConnectionString() {
        logger.info("Building connection string...");
        String dbUsername = System.getProperty("DB_USER");
        String dbPassword = System.getProperty("DB_PASSWORD");
        String dbHost = System.getProperty("DB_HOST");
        String dbPort = System.getProperty("DB_PORT");
        String dbName = System.getProperty("DB_NAME");
        String authSource = System.getProperty("AUTH_SOURCE");
        String connectionString = String.format("mongodb://%s:%s@%s:%s/%s%s", dbUsername, dbPassword, dbHost, dbPort, dbName, authSource);
        logger.info("Connection string built successfully.");
        return connectionString;
    }

    public void insertDocument(String collectionName, Document document) {
        logger.info("Inserting document into collection: {}", collectionName);
        MongoCollection<Document> collection = database.getCollection(collectionName);
        collection.insertOne(document);
        logger.info("Document inserted successfully.");
    }

    public FindIterable<Document> findDocument(String collectionName, Bson filter) {
        logger.info("Finding document in collection: {}", collectionName);
        MongoCollection<Document> collection = database.getCollection(collectionName);
        FindIterable<Document> result = collection.find(filter);
        logger.info("Document found successfully.");
        return result;
    }

    public void insertMetadata(Blob blob) {
        Document document = new Document();
        document.append("id", blob.getId());
        document.append("size", blob.getSize());
        document.append("timestamp", blob.getCreatedAt());
        insertDocument("metadata", document);
    }

    public void close() {
        logger.info("Closing MongoDBClient...");
        mongoClient.close();
        logger.info("MongoDBClient closed successfully.");
    }
}