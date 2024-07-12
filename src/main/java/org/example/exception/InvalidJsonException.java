package org.example.exception;

import com.fasterxml.jackson.core.JsonProcessingException;

public class InvalidJsonException extends JsonProcessingException {
    public InvalidJsonException(String msg) {
        super(msg);
    }
}