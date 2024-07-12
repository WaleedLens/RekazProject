package org.example.exception;

public class BlobNotFoundException extends RuntimeException {
    public BlobNotFoundException(String id) {
        super("Blob with id " + id + " not found");
    }
}