package com.cinx.course.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Component;

@Component
public class JsonConverter {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public JsonConverter() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    public static String toJson(Object obj) {
        try {
            System.out.println("Converting object to JSON: " + obj);
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert object to JSON" + e.getMessage(), e);
        }
    }
}
