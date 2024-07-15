package org.example.core;

import com.google.inject.AbstractModule;
import org.example.services.*;
import org.example.services.S3StorageService;

/**
 * This class is responsible for configuring the storage service of the application.
 * It extends the AbstractModule class from Google Guice, which allows for dependency injection.
 * The storage service is chosen based on the "STORAGE_BACKEND" system property.
 */
public class StorageModule extends AbstractModule {
    /**
     * Configures the storage service of the application.
     * The storage service is chosen based on the "STORAGE_BACKEND" system property.
     * The possible values for "STORAGE_BACKEND" are "s3", "database", "local", and "ftp".
     * If the value of "STORAGE_BACKEND" is not one of these, an IllegalArgumentException is thrown.
     */
    @Override
    protected void configure() {
        String storageBackend = System.getProperty("STORAGE_BACKEND");

        switch (storageBackend) {
            case "s3":
                // If "STORAGE_BACKEND" is "s3", the S3StorageService is used.
                bind(StorageService.class).to(S3StorageService.class);
                break;
            case "database":
                // If "STORAGE_BACKEND" is "database", the DatabaseStorageService is used.
                bind(StorageService.class).to(DatabaseStorageService.class);
                break;
            case "local":
                // If "STORAGE_BACKEND" is "local", the LocalFileStorageService is used.
                bind(StorageService.class).to(LocalFileStorageService.class);
                break;
            case "ftp":
                // If "STORAGE_BACKEND" is "ftp", the FtpStorageService is used.
                bind(StorageService.class).to(FtpStorageService.class);
                break;
            default:
                // If "STORAGE_BACKEND" is not one of the expected values, an IllegalArgumentException is thrown.
                throw new IllegalArgumentException("Invalid storage backend: " + storageBackend);
        }
    }
}