package org.example.services;


import org.example.aws.S3Client;
import org.example.model.Blob;
import org.example.model.BlobDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S3StorageService implements StorageService {
    private static final Logger logger = LoggerFactory.getLogger(S3StorageService.class);

    @Override
    public void saveBlob(BlobDto blobDto) {
        S3Client s3Client = new S3Client();
        s3Client.putObjectToS3(blobDto.getId(), blobDto.getData());
    }

    @Override
    public Blob getBlob(String id) {
        S3Client s3Client = new S3Client();
        s3Client.getObjectFromS3(id);
        return new Blob("","");
    }
}
