package org.example.services;

import com.google.inject.Inject;
import org.bson.Document;
import org.example.core.MongoDBClient;
import org.example.exception.BlobNotFoundException;
import org.example.model.Blob;
import org.example.model.BlobDto;
import org.example.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;

/**
 * This class provides an implementation of the StorageService interface.
 * It uses MongoDB as the storage backend.
 */
public class DatabaseStorageService implements StorageService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseStorageService.class);
    private final MongoDBClient mongoClient;

    /**
     * Constructs a new DatabaseStorageService.
     *
     * @param mongoClient the MongoDB client to be used for database operations
     */
    @Inject
    public DatabaseStorageService(MongoDBClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    /**
     * Saves a blob to the database.
     *
     * @param blobDto the blob to be saved
     */
    @Override
    public void saveBlob(BlobDto blobDto) {
        logger.info("Saving blob with id: {}", blobDto.getId());
        Document document = new Document();
        Blob blob = new Blob(blobDto.getId(), blobDto.getData(), FileUtils.getBlobSize(blobDto.getData()));
        document.append("id", blob.getId());
        document.append("data", blob.getData());
        mongoClient.insertDocument("blobs", document);
        mongoClient.insertMetadata(blob);
        logger.info("Blob with id: {} saved successfully.", blobDto.getId());
    }

    /**
     * Retrieves a blob from the database.
     *
     * @param id the id of the blob to be retrieved
     * @return the retrieved blob
     * @throws BlobNotFoundException if no blob with the given id is found
     */
    @Override
    public Blob getBlob(String id) {
        logger.info("Retrieving blob with id: {}", id);
        Document document = mongoClient.findDocument("blobs", new Document("id", id)).first();
        if (document == null) {
            logger.error("No blob found with id: {}", id);
            throw new BlobNotFoundException(id);
        }
        Blob blob = new Blob(document.getString("id"), document.getString("data"));
        Document metadataDocument = mongoClient.findDocument("metadata", new Document("id", id)).first();
        if (metadataDocument != null) {
            blob.setSize(metadataDocument.getInteger("size"));
            blob.setCreatedAt((Timestamp) metadataDocument.getDate("timestamp"));
        }
        logger.info("Blob with id: {} retrieved successfully.", id);
        return blob;
    }
}