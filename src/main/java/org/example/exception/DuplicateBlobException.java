package org.example.exception;

public class DuplicateBlobException extends RuntimeException {
    public DuplicateBlobException(String id) {
        super("A blob with id: " + id + " already exists.");
    }
}