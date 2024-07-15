package org.example.services;

import com.google.inject.Inject;
import org.bson.Document;
import org.example.aws.S3Client;
import org.example.database.MongoDBClient;
import org.example.model.Blob;
import org.example.model.BlobDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;

/**
 * This class is responsible for managing the S3 storage service.
 * It provides methods to save blobs and get blobs.
 */
public class S3StorageService implements StorageService {
    private static final Logger logger = LoggerFactory.getLogger(S3StorageService.class);

    private final MongoDBClient mongoClient;

    /**
     * Constructor for the S3StorageService.
     * Initializes the mongoClient with the provided MongoDBClient.
     *
     * @param mongoClient The MongoDBClient.
     */
    @Inject
    public S3StorageService(MongoDBClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    /**
     * Saves a blob to the S3 bucket and inserts its metadata into the "metadata" collection.
     *
     * @param blobDto The blob data transfer object containing the blob id and data.
     */
    @Override
    public void saveBlob(BlobDto blobDto) {
        S3Client s3Client = new S3Client();
        s3Client.putObjectToS3(blobDto.getId(), blobDto.getData());
        Blob blob = new Blob(blobDto.getId(), blobDto.getData(), blobDto.getData().length());
        mongoClient.insertMetadata(blob);
    }

    /**
     * Retrieves a blob from the S3 bucket and its metadata from the "metadata" collection.
     *
     * @param id The id of the blob.
     * @return The retrieved blob.
     */
    @Override
    public Blob getBlob(String id) {
        S3Client s3Client = new S3Client();
        String data = s3Client.getObjectFromS3(id);
        Document metadataDocument = mongoClient.findDocument("metadata", new Document("id", id)).first();
        Blob blob = new Blob(id, data);
        if (metadataDocument != null) {
            blob.setSize(metadataDocument.getInteger("size"));
            blob.setCreatedAt(new Timestamp(metadataDocument.getDate("timestamp").getTime()));
        }
        return blob;
    }
}