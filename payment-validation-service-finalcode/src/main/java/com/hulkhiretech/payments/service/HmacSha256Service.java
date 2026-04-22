package com.hulkhiretech.payments.service;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.hulkhiretech.payments.constant.Constant;
import com.hulkhiretech.payments.constant.ErrorCodeEnum;
import com.hulkhiretech.payments.exception.PaymentValidationException;
import com.hulkhiretech.payments.pojo.PaymentRequest;
import com.hulkhiretech.payments.util.JsonUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class HmacSha256Service {
	
	@Value("${hmac.secret-key}")
	private String secretKey;
	
	private final JsonUtil jsonUtil;
	
	public String computeHmacSha256(String jsonInput) {
		log.info("Computing HMAC-SHA256 for input: {}", jsonInput);
		
        try {
            // Create a SecretKeySpec object from the secret key
            SecretKeySpec keySpec = new SecretKeySpec(
            		secretKey.getBytes(StandardCharsets.UTF_8), 
            		Constant.HMAC_SHA256);

            // Initialize the HMAC-SHA256 Mac instance
            Mac mac = Mac.getInstance(Constant.HMAC_SHA256);
            mac.init(keySpec);

            // Compute the HMAC-SHA256 signature
            byte[] signatureBytes = mac.doFinal(jsonInput.getBytes(StandardCharsets.UTF_8));

            // Encode the signature in Base64
            String hmacSignature = Base64.getEncoder().encodeToString(signatureBytes);

            log.info("HMAC-SHA256 Signature: {}", hmacSignature);
            return hmacSignature;
        
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error computing HMAC-SHA256 signature", e);
            
            throw new PaymentValidationException(
            		ErrorCodeEnum.HMAC_COMPUTATION_ERROR.getErrorCode(),
            		ErrorCodeEnum.HMAC_COMPUTATION_ERROR.getErrorMessage(),
            		HttpStatus.INTERNAL_SERVER_ERROR);
        }
	}

	public String isHmacSignatureValid(
			String jsonString, String headerHmacSignature) {
		
		if(headerHmacSignature == null || headerHmacSignature.isEmpty()) {
			log.error("Missing HMAC signature in request header");
			throw new PaymentValidationException(
					ErrorCodeEnum.MISSING_HMAC.getErrorCode(),
					ErrorCodeEnum.MISSING_HMAC.getErrorMessage(),
					HttpStatus.UNAUTHORIZED);
		}

		String calculatedHmac = computeHmacSha256(jsonString);

		if(!calculatedHmac.equals(headerHmacSignature)) {
			log.error("HMAC validation failed. Calculated: {}, Received: {}",
					calculatedHmac, headerHmacSignature);
			throw new PaymentValidationException(
					ErrorCodeEnum.INVALID_HMAC.getErrorCode(),
					ErrorCodeEnum.INVALID_HMAC.getErrorMessage(),
					HttpStatus.UNAUTHORIZED);
		}
		return calculatedHmac;
	}

	
}
