package com.hulkhiretech.payments.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hulkhiretech.payments.pojo.PaymentRequest;
import com.hulkhiretech.payments.pojo.PaymentResponse;
import com.hulkhiretech.payments.service.interfaces.PaymentService;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/payments")
@Slf4j
@RequiredArgsConstructor
@RefreshScope
public class PaymentController {

    private final PaymentService paymentService;
    
    @Value("${mytestkey}")
    private String myTestKey;
    
    @PostMapping
    public PaymentResponse createPayment(
            @Valid @RequestBody 
            PaymentRequest paymentRequest) {
        log.info("Creating payment... paymentRequest: {}", paymentRequest);
        
        PaymentResponse serviceResponse = paymentService
        		.validateAndCreatePayment(
        				paymentRequest);
        log.info("Payment created: {}", serviceResponse);
        
		return serviceResponse;
    }
    
    @GetMapping
    public String getPaymentStatus() {  
    	log.info("Getting payment status... This endpoint is under construction.");
    	return "Payment status endpoint is under construction.";
    }
    
    @PostConstruct
    public void init() {   
    	// print myTestKey
    	log.info("****myTestKey value: {}", myTestKey);  
    }
}