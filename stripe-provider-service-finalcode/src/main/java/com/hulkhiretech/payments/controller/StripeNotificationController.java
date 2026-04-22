package com.hulkhiretech.payments.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hulkhiretech.payments.service.StripeNotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/stripe/notifications")
@RequiredArgsConstructor
public class StripeNotificationController {

    private final StripeNotificationService stripeNotificationService;
    
    @PostMapping
    public ResponseEntity<Void> process(
    		@RequestHeader(value = "Stripe-Signature", required = false) 
    		String stripeSignature,
    		@RequestBody String jsonRequest) {
        // No business logic here — delegate to service and acknowledge with 200 OK
        log.info("Received Stripe notification payload of length {}", 
                jsonRequest != null ? jsonRequest.length() : 0);
        log.info("Stripe-Signature header: {}", stripeSignature);

        stripeNotificationService.processNotification(stripeSignature, jsonRequest);

        return ResponseEntity.ok().build();
    }

}