package org.example.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.exception.InvalidRequestException;

import java.io.IOException;
import java.io.InputStream;

public class ParsingUtils {

    public static <T> T parseJson(InputStream in, Class<T> clazz) throws IOException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(in, clazz);
        } catch (JsonProcessingException e) {
            // Handle invalid JSON format (e.g., log, throw custom exception)

            throw new InvalidRequestException("Invalid JSON format", e);
        }
    }

    public static <T> T parseObject(String json, Class<T> clazz) throws IOException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            // Handle invalid JSON format (e.g., log, throw custom exception)
            throw new InvalidRequestException("Invalid JSON format", e);
        }
    }





}
