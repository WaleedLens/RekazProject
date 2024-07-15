package org.example.database;

import com.google.inject.Singleton;
import com.mongodb.ErrorCategory;
import com.mongodb.MongoWriteException;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.example.exception.DuplicateBlobException;
import org.example.model.Blob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for managing the MongoDB client.
 * It provides methods to initialize the client, build the connection string, insert documents, find documents, insert metadata, and close the client.
 */
@Singleton
public class MongoDBClient {
    private static final Logger logger = LoggerFactory.getLogger(MongoClient.class);

    private final MongoClient mongoClient;
    private final MongoDatabase database;

    /**
     * Constructor for the MongoDBClient.
     * Initializes the mongoClient and database, and migrates the database.
     */
    public MongoDBClient() {
        logger.info("Initializing MongoDBClient...");
        String connectionString = buildConnectionString();
        this.mongoClient = MongoClients.create(connectionString);
        String dbName = System.getProperty("DB_NAME");
        this.database = mongoClient.getDatabase(dbName);
        DatabaseMigration databaseMigration = new DatabaseMigration(mongoClient);
        databaseMigration.migrate();
        logger.info("MongoDBClient initialized successfully.");
    }

    /**
     * Constructor for the MongoDBClient for testing.
     * Initializes the mongoClient and database with the provided MongoClient and a test database.
     *
     * @param mongoClient The MongoClient.
     */
    public MongoDBClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
        String dbName = "test_db";
        this.database = mongoClient.getDatabase(dbName);
    }

    /**
     * Builds the connection string for the MongoDB client.
     *
     * @return The connection string.
     */
    private String buildConnectionString() {
        logger.info("Building connection string...");
        String dbUsername = System.getProperty("DB_USER");
        String dbPassword = System.getProperty("DB_PASSWORD");
        String dbHost = System.getProperty("DB_HOST");
        String dbName = System.getProperty("DB_NAME");
        String authSource = System.getProperty("AUTH_SOURCE");
        String connectionString = String.format("mongodb://%s:%s@%s/%s%s", dbUsername, dbPassword, dbHost, dbName, authSource);
        logger.info("Connection string: {}", connectionString);
        logger.info("Connection string built successfully.");
        return connectionString;
    }

    /**
     * Inserts a document into the specified collection.
     * If a document with the same id already exists in the collection, a DuplicateBlobException is thrown.
     *
     * @param collectionName The name of the collection.
     * @param document The document to insert.
     */
    public void insertDocument(String collectionName, Document document) {
        logger.info("Inserting document into collection: {}", collectionName);
        MongoCollection<Document> collection = database.getCollection(collectionName);
        try {
            collection.insertOne(document);
            logger.info("Document inserted successfully.");
        } catch (MongoWriteException e) {
            if (e.getError().getCategory() == ErrorCategory.DUPLICATE_KEY) {
                String id = document.getString("id");
                logger.error("A document with id: {} already exists in collection: {}", id, collectionName);
                throw new DuplicateBlobException(id);
            }
            throw e;
        }
    }

    /**
     * Finds a document in the specified collection with the provided filter.
     *
     * @param collectionName The name of the collection.
     * @param filter The filter to use when finding the document.
     * @return The found document.
     */
    public FindIterable<Document> findDocument(String collectionName, Bson filter) {
        logger.info("Finding document in collection: {}", collectionName);
        MongoCollection<Document> collection = database.getCollection(collectionName);
        FindIterable<Document> result = collection.find(filter);
        logger.info("Document found successfully.");
        return result;
    }

    /**
     * Inserts metadata for the provided blob into the "metadata" collection.
     *
     * @param blob The blob for which to insert metadata.
     */
    public void insertMetadata(Blob blob) {
        Document document = new Document();
        document.append("id", blob.getId());
        document.append("size", blob.getSize());
        document.append("timestamp", blob.getCreatedAt());
        insertDocument("metadata", document);
    }

    /**
     * Closes the MongoDB client.
     */
    public void close() {
        logger.info("Closing MongoDBClient...");
        mongoClient.close();
        logger.info("MongoDBClient closed successfully.");
    }

    /**
     * Returns the mongoClient.
     *
     * @return The mongoClient.
     */
    public MongoClient getMongoClient() {
        return mongoClient;
    }

    /**
     * Returns the database.
     *
     * @return The database.
     */
    public MongoDatabase getDatabase() {
        return database;
    }
}