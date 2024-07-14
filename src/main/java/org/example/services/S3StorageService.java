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

public class S3StorageService implements StorageService {
    private static final Logger logger = LoggerFactory.getLogger(S3StorageService.class);

    private final MongoDBClient mongoClient;


    @Inject
    public S3StorageService(MongoDBClient mongoClient) {
        this.mongoClient = mongoClient;
    }


    @Override
    public void saveBlob(BlobDto blobDto) {
        S3Client s3Client = new S3Client();
        s3Client.putObjectToS3(blobDto.getId(), blobDto.getData());
        Blob blob = new Blob(blobDto.getId(), blobDto.getData(), blobDto.getData().length());
        mongoClient.insertMetadata(blob);

    }

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
