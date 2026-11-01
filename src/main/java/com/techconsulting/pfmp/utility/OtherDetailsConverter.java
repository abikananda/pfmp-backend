package com.techconsulting.pfmp.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Converter
public class OtherDetailsConverter implements AttributeConverter<Map<String, Object>, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        log.debug("Converting Map to database column");
        if (attribute == null || attribute.isEmpty()) {
            log.debug("Attribute is null or empty, returning empty JSON");
            return "{}";
        }
        try {
            String result = objectMapper.writeValueAsString(attribute);
            log.debug("Successfully converted Map to JSON");
            return result;
        } catch (JsonProcessingException e) {
            log.error("Error converting Map to JSON", e);
            throw new IllegalArgumentException("Error converting Map to JSON", e);
        }
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        log.debug("Converting database column to Map");
        if (dbData == null || dbData.trim().isEmpty()) {
            log.debug("Database data is null or empty, returning empty HashMap");
            return new HashMap<>();
        }
        try {
            Map<String, Object> result = objectMapper.readValue(dbData, new TypeReference<Map<String, Object>>() {});
            log.debug("Successfully converted JSON to Map with {} entries", result.size());
            return result;
        } catch (IOException e) {
            log.error("Error converting JSON to Map", e);
            throw new IllegalArgumentException("Error converting JSON to Map", e);
        }
    }
}
