package org.example.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.exception.InvalidJsonException;
import org.example.exception.InvalidRequestException;
import org.example.model.Blob;
import org.example.model.BlobDto;
import org.example.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;

public class StorageService {
    private static final Logger logger = LoggerFactory.getLogger(StorageService.class);


    public void saveBlob(BlobDto blobDto) {
        try {
            validateBlobData(blobDto);
            isValidBase64(blobDto.getData());
            Blob blob = new Blob(blobDto.getId(), blobDto.getData(), FileUtils.getBlobSize(blobDto.getData()));
            logger.info("Blob saved successfully" + blob.toString());
            logger.info("HEYY ===>{}",System.getProperty("STORAGE_BACKEND"));
        } catch (JsonProcessingException e) {

            logger.error("Error parsing JSON ", e);

        }

    }


    public Blob getBlob(String id) {
        System.out.println("Getting blob with id: " + id);
        return null;
    }


    private void isValidBase64(String base64String) throws IllegalArgumentException {
        try {
            Base64.getDecoder().decode(base64String);
            logger.info("Base64 data is valid");
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException("Invalid Base64 data", e);
        }
    }


    public void validateBlobData(BlobDto blobData) throws InvalidJsonException {
        if (blobData.getId() == null || blobData.getData() == null) {
            throw new InvalidJsonException("Invalid JSON: id and data are required");
        }
    }


}
