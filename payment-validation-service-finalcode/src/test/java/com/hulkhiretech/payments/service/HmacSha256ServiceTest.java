package com.hulkhiretech.payments.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hulkhiretech.payments.pojo.PaymentRequest;
import com.hulkhiretech.payments.service.data.TestDataBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HmacSha256ServiceTest {

    @Test
    public void testComputeHmacSha256ProducesDeterministic32ByteHmac() {
        HmacSha256Service service = new HmacSha256Service(null);
        
        try {
        	// Set the private secretKey field using reflection since we're not running inside Spring
        	Field secretKeyField = HmacSha256Service.class.getDeclaredField("secretKey");
        	secretKeyField.setAccessible(true);
        	// Use a deterministic test key
        	secretKeyField.set(service, "THIS_IS_MY_SECRET");
        	
        	log.info("Secretkey set");
        } catch (Exception e) {
			e.printStackTrace();
        }


        PaymentRequest requestObj = TestDataBuilder.buildPaymentRequest();
        //Use Jackson ObjectMapper to convert the PaymentRequest to JSON string
        
        ObjectMapper objectMapper = new ObjectMapper();
        String dummyJson = null;
        try {
			dummyJson = objectMapper.writeValueAsString(requestObj);
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        // Convert the PaymentRequest to JSON string (using a simple approach here)
        String signature = service.computeHmacSha256(dummyJson);

        // Should not be null
        assertNotNull(signature, "HMAC signature should not be null");

        // Must be valid Base64
        byte[] decoded = Base64.getDecoder().decode(signature);

        // HMAC-SHA256 produces 32 bytes
        assertEquals(32, decoded.length, 
        		"Decoded HMAC should be 32 bytes (SHA-256)");

        // Call again with same input to ensure deterministic output
        String signature2 = service.computeHmacSha256(dummyJson);
        assertEquals(signature, signature2, 
        		"HMAC should be deterministic for same input and key");
    }

}