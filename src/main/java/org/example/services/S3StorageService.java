package org.example.services;


import org.example.model.Blob;
import org.example.model.BlobDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S3StorageService implements StorageService {
    private static final Logger logger = LoggerFactory.getLogger(S3StorageService.class);

    @Override
    public void saveBlob(BlobDto blobDto) {
        logger.info("Its S3!!!");
    }

    @Override
    public Blob getBlob(String id) {
        return null;
    }
}
