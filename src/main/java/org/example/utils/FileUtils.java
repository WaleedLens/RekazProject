package org.example.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class FileUtils {

    public static int getBlobSize(String data) {
        if (data == null || data.isEmpty()) {
            return 0;
        }
        try {
            return data.getBytes().length;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Base64 data", e);
        }
    }

    public static String encodeStringToBase64(String data) {
        return Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }

    public static String decodeBase64ToString(String base64Data) {
        byte[] decodedBytes = Base64.getDecoder().decode(base64Data);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    public static String hashStringContent(String base64Data) throws NoSuchAlgorithmException {
        byte[] data = Base64.getDecoder().decode(base64Data);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data);
        return bytesToHex(hash);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static String hash(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(text.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] decodeBase64(String base64Str) {
        return Base64.getDecoder().decode(base64Str);
    }

}
