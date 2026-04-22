package com.hulkhiretech.payments.service.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hulkhiretech.payments.http.HttpRequest;
import com.hulkhiretech.payments.http.HttpServiceEngine;
import com.hulkhiretech.payments.pojo.CreatePaymentReq;
import com.hulkhiretech.payments.pojo.PaymentResponse;
import com.hulkhiretech.payments.service.ValidationService;
import com.hulkhiretech.payments.service.helper.CreatePaymentHelper;
import com.hulkhiretech.payments.service.interfaces.PaymentService;
import com.hulkhiretech.payments.stripe.CheckoutSessionResponse;
import com.hulkhiretech.payments.utl.JsonUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

	private final HttpServiceEngine httpServiceEngine;

	private final CreatePaymentHelper createPaymentHelper;

	private final JsonUtil jsonUtil;

	private final ValidationService validationService;

	/**
	 * Creates a Stripe payment session and returns a mapped PaymentResponse.
	 *
	 * Flow:
	 * 1. Logs and validates the incoming CreatePaymentReq.
	 * 2. Prepares Stripe session creation HTTP request.
	 * 3. Executes the HTTP call using HttpServiceEngine.
	 * 4. Processes Stripe response into CheckoutSessionResponse.
	 * 5. Maps CheckoutSessionResponse to internal PaymentResponse.
	 * 6. Returns the final PaymentResponse.
	 */
	@Override
	public PaymentResponse createPayment(CreatePaymentReq createPaymentReq) {
		log.info("Processing payment creation logic... createPaymentReq: {}", 
				createPaymentReq);

		// Validate request before proceeding
		validationService.isValid(createPaymentReq);

		HttpRequest httpRequest = createPaymentHelper
				.prepareStripeCreateSessionRequest(createPaymentReq);

		ResponseEntity<String> httpResponse = httpServiceEngine.makeHttpCall(httpRequest);
		log.info("Received response from HttpServiceEngine: {}", httpResponse);

		CheckoutSessionResponse checkoutSession = createPaymentHelper
				.processStripeResponse(httpResponse);
		log.info("Processed Stripe response and obtained CheckoutSessionResponse: {}", checkoutSession);

		PaymentResponse paymentResponse = mapCheckoutSessionToPaymentResponse(checkoutSession);
		log.info("Mapped PaymentResponse: {}", paymentResponse);

		return paymentResponse;
	}


	/**
	 * Write a map method to take CheckoutSessionResponse 
	 * and convert it to PaymentResponse which is 
	 * our internal response object. This way we are not
	 */
	public PaymentResponse mapCheckoutSessionToPaymentResponse(
			CheckoutSessionResponse checkoutSession) {

		if (checkoutSession == null) {
			log.warn("mapCheckoutSessionToPaymentResponse called with null checkoutSession");
			return null;
		}

		PaymentResponse paymentResponse = new PaymentResponse();
		paymentResponse.setStripeSessionId(checkoutSession.getId());
		paymentResponse.setHostedPageUrl(checkoutSession.getUrl());

		log.info("Mapped CheckoutSessionResponse to PaymentResponse: {}", paymentResponse);
		return paymentResponse;
	}
}