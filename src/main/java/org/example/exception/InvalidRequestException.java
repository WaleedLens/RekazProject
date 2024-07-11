package org.example.exception;

public class InvalidRequestException extends RuntimeException {


    public InvalidRequestException(String message,Exception e){
        super(message,e);
    }

}
