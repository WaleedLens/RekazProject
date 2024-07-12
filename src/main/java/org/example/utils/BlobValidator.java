package org.example.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.exception.InvalidJsonException;
import org.example.exception.InvalidRequestException;
import org.example.model.BlobDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;

public class BlobValidator {
    private static final Logger logger = LoggerFactory.getLogger(BlobValidator.class);

    public static void validateBlobData(BlobDto blobData) throws InvalidJsonException {
        if (blobData.getId() == null || blobData.getData() == null) {
            logger.error("Invalid JSON: id and data are required");
            throw new InvalidJsonException("Invalid JSON: id and data are required");
        }
    }

    public static void isValidBase64(String base64String) throws InvalidRequestException {
        try {
            Base64.getDecoder().decode(base64String);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid Base64 data", e);
            throw new InvalidRequestException("Invalid Base64 data", e);

        }
    }
}