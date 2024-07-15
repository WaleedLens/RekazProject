package org.example.core;

import com.google.inject.AbstractModule;
import org.example.services.*;
import org.example.services.S3StorageService;

public class StorageModule extends AbstractModule {
    @Override
    protected void configure() {
        String storageBackend = System.getProperty("STORAGE_BACKEND");

        switch (storageBackend) {
            case "aws":
                bind(StorageService.class).to(S3StorageService.class);
                break;
            case "database":
                bind(StorageService.class).to(DatabaseStorageService.class);
                break;
            case "local":
                bind(StorageService.class).to(LocalFileStorageService.class);
                break;
            case "ftp":
                bind(StorageService.class).to(FtpStorageService.class);
                break;
            default:
                throw new IllegalArgumentException("Invalid storage backend: " + storageBackend);
        }
    }
}