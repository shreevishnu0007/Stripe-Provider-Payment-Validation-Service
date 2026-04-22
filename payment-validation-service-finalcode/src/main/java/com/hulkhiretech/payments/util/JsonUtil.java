package com.hulkhiretech.payments.util;

import java.util.LinkedHashMap;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JsonUtil {

    private final ObjectMapper objectMapper;

    /**
     * Convert JSON string to Java object of provided type. Returns null on error.
     */
    public <T> T convertJsonToObject(String json, Class<T> clazz) {
        if (json == null) {
            log.debug("convertJsonToObject called with null json");
            return null;
        }
        if (clazz == null) {
            log.debug("convertJsonToObject called with null clazz");
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("Failed to convert JSON to {}: {}", clazz.getSimpleName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * Convert Java object to JSON string. Returns null on error.
     */
    public String convertObjectToJson(Object obj) {
        if (obj == null) {
            log.debug("convertObjectToJson called with null object");
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Failed to convert object to JSON: {}", e.getMessage(), e);
            return null;
        }
    }
    
    public String prepareFormattedJson(String body) {
		if (body == null || body.isBlank()) {
			return "";
		}
		
		try {
			// Parse JSON while preserving order
			LinkedHashMap<String, Object> map =
					objectMapper.readValue(body, LinkedHashMap.class);
			// Serialize back to JSON
			return objectMapper.writeValueAsString(map);
		} catch (Exception e) {
			log.error("Error while formatting JSON body: {}", e.getMessage(), e);
			return null;
		}
	}
}