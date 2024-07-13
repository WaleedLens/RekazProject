package org.example.database;

import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class DatabaseMigration {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseMigration.class);

    private final MongoClient mongoClient;

    public DatabaseMigration(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public void migrate() {
        MongoDatabase database = mongoClient.getDatabase(System.getProperty("DB_NAME"));

        if (database.listCollectionNames().into(new ArrayList<>()).contains("blobs")) {
            logger.info("Collection 'blobs' already exists, skipping migration.");
        } else {
            logger.info("Collection 'blobs' does not exist, starting migration...");
            startMigration("blobs");
            addUniqueIndex("blobs");
        }

        if (database.listCollectionNames().into(new ArrayList<>()).contains("metadata")) {
            logger.info("Collection 'metadata' already exists, skipping migration.");
        } else {
            logger.info("Collection 'metadata' does not exist, starting migration...");
            startMigration("metadata");
            addUniqueIndex("metadata");
        }
    }

    private void startMigration(String collectionName) {
        try {
            MongoDatabase database = mongoClient.getDatabase(System.getProperty("DB_NAME"));
            database.createCollection(collectionName);
            logger.info("Collection '{}' created successfully.", collectionName);
        } catch (MongoCommandException e) {
            logger.error("Failed to create collection '{}'.", collectionName, e);
            throw e;
        }
    }

    private void addUniqueIndex(String collectionName) {
        try {
            MongoDatabase database = mongoClient.getDatabase(System.getProperty("DB_NAME"));
            MongoCollection<Document> collection = database.getCollection(collectionName);
            IndexOptions indexOptions = new IndexOptions().unique(true);
            collection.createIndex(Indexes.ascending("id"), indexOptions);
            logger.info("Unique index on 'id' added to collection '{}'.", collectionName);
        } catch (MongoCommandException e) {
            logger.error("Failed to add unique index to collection '{}'.", collectionName, e);
            throw e;
        }
    }
}