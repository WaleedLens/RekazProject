package org.example.services;

import org.example.exception.BlobNotFoundException;
import org.example.exception.FileAlreadyExistsException;
import org.example.model.Blob;
import org.example.model.BlobDto;
import org.example.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class LocalFileStorageService implements StorageService {
    private static final Logger logger = LoggerFactory.getLogger(LocalFileStorageService.class);
    private final Path path;

    public LocalFileStorageService() {
        String blobPath = System.getProperty("LOCAL_STORAGE_PATH");
        this.path = Path.of(blobPath);
        createStorageDirectory();
    }

    @Override
    public void saveBlob(BlobDto blobDto) {
        logger.info("Saving blob with id {}", blobDto.getId());

        Blob blob = new Blob(blobDto.getId(), blobDto.getData(), FileUtils.getBlobSize(blobDto.getData()));
        logger.info("Blob size: {}", blob.getSize());
        createFile(blob);
    }

    private void createFile(Blob blob) {
        Path filePath = path.resolve(blob.getId());
        if (Files.exists(filePath)) {
            throw new FileAlreadyExistsException(blob.getId());
        }
        try {
            byte[] data = Base64.getDecoder().decode(blob.getData());
            Files.write(filePath, data);
            logger.info("Created file at {}", filePath);
        } catch (IOException e) {
            logger.error("Failed to create file", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Blob getBlob(String id) {
        Path filePath = path.resolve(id);
        if (!Files.exists(filePath)) {
            throw new BlobNotFoundException(id);
        }
        try {
            byte[] data = Files.readAllBytes(filePath);
            String encodedData = Base64.getEncoder().encodeToString(data);
            return new Blob(id, encodedData, data.length);
        } catch (IOException e) {
            logger.error("Failed to read file", e);
            throw new RuntimeException(e);
        }
    }

    private void createStorageDirectory() {
        if (!Files.exists(path)) {
            try {
                logger.info("Creating storage directory at {}", path);
                Files.createDirectory(path);
            } catch (IOException e) {
                logger.error("Failed to create storage directory", e);
                throw new RuntimeException(e);
            }
        }
    }
}