package com.hulkhiretech.payments.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hulkhiretech.payments.pojo.CreatePaymentReq;
import com.hulkhiretech.payments.pojo.PaymentResponse;
import com.hulkhiretech.payments.service.interfaces.PaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/payments")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Payments", description = "APIs to create and manage Stripe payment sessions")
public class PaymentController {

	private final PaymentService paymentService;

	@PostMapping
	@Operation(summary = "Create Stripe checkout session",
	description = "Creates a Stripe checkout session for the provided line items and returns a response containing the Stripe session id and hosted payment page URL.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Payment session created successfully",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaymentResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid request payload or validation failure", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
	})
	public PaymentResponse createPayment(
			@RequestBody CreatePaymentReq createPaymentReq) {
		log.info("Creating payment... createPaymentReq: {}", createPaymentReq);

		PaymentResponse paymentResponse = paymentService.createPayment(createPaymentReq);
		log.info("Payment created: {}", paymentResponse);

		return paymentResponse;
	}

}