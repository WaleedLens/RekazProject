package org.example.exception;

public class FileAlreadyExistsException extends RuntimeException {
    public FileAlreadyExistsException(String id) {
        super("File with id " + id + " already exists");
    }
}