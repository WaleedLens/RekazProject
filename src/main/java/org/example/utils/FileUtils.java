package org.example.utils;

import java.util.Base64;

public class FileUtils {

    public static int getBlobSize(String base64Data) {
        if (base64Data == null || base64Data.isEmpty()) {
            return 0;
        }
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64Data);
            return decodedBytes.length;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Base64 data", e);
        }
    }
}
