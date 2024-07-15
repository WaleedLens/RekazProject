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

/**
 * This class is responsible for managing the database migrations.
 * It provides methods to migrate the database and add unique indexes to collections.
 */
public class DatabaseMigration {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseMigration.class);

    private final MongoClient mongoClient;

    /**
     * Constructor for the DatabaseMigration.
     * Initializes the mongoClient with the provided MongoClient.
     *
     * @param mongoClient The MongoClient.
     */
    public DatabaseMigration(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    /**
     * Migrates the database.
     * If the collections "blobs" and "metadata" do not exist, they are created and a unique index is added to them.
     */
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

    /**
     * Starts the migration for the provided collection.
     * If the collection cannot be created, a MongoCommandException is thrown.
     *
     * @param collectionName The name of the collection.
     */
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

    /**
     * Adds a unique index to the provided collection.
     * If the index cannot be added, a MongoCommandException is thrown.
     *
     * @param collectionName The name of the collection.
     */
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