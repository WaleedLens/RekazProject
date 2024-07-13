package org.example.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.example.exception.InvalidRequestException;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;

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

    public static String objectToJson(Object object) throws IOException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"));
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            // Handle invalid JSON format (e.g., log, throw custom exception)
            throw new InvalidRequestException("Invalid JSON format", e);
        }
    }


}
