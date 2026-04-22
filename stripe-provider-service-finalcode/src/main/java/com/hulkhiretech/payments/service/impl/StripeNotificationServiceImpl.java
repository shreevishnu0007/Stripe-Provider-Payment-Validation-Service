package com.hulkhiretech.payments.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.hulkhiretech.payments.constant.ErrorCodeEnum;
import com.hulkhiretech.payments.exception.StripeProviderException;
import com.hulkhiretech.payments.service.StripeNotificationService;
import com.hulkhiretech.payments.stripe.CheckoutSessionResponse;
import com.hulkhiretech.payments.utl.JsonUtil;
import com.stripe.model.Event;
import com.stripe.net.Webhook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StripeNotificationServiceImpl implements StripeNotificationService {

	@Value("${stripe.webhook.endpoint-secret}")
	private String endpointSecret;

	List<String> successEvents = List.of(
			"checkout.session.completed",
			"checkout.session.async_payment_succeeded"
			);

	List<String> failedEvents = List.of(
			"checkout.session.async_payment_failed"
			);

	private final JsonUtil jsonUtil;

	@Override
	public void processNotification(
			String stripeSignature, String jsonRequest) {
		// No business logic per instructions. Just log at debug level.
		log.info("StripeNotificationServiceImpl.processNotification "
				+ "called with payload length: {}", jsonRequest != null ? jsonRequest.length() : 0);

		log.info("Stripe-Signature header: {}", stripeSignature);

		// if signature is missing or jsonRequest is empty, then throw StripeProviderException with 400 Bad Request status code
		if ((stripeSignature == null || stripeSignature.isEmpty())
				|| (jsonRequest == null || jsonRequest.isEmpty())) {
			log.error("Missing Stripe-Signature header or empty request body");

			throw new StripeProviderException(
					ErrorCodeEnum.MISSING_STRIPE_SIGNATURE_OR_EMPTY_REQUEST.getErrorCode(),
					ErrorCodeEnum.MISSING_STRIPE_SIGNATURE_OR_EMPTY_REQUEST.getErrorMessage(),
					HttpStatus.BAD_REQUEST);
		}

		Event event = isSignatureValid(stripeSignature, jsonRequest);

		log.info("Stripe signature valid id:{}|type:{}", 
				event.getId(),
				event.getType());

		// TODO in future you can save all events to database.

		//if the even doesn't belong to success or failed, return
		if (!successEvents.contains(event.getType()) 
				&& !failedEvents.contains(event.getType())) {
			log.info("Received Stripe event of type {} which is not in success or failed events list. Ignoring.", event.getType());
			return;
		}


		if (successEvents.contains(event.getType())) {
			log.info("Received Stripe event of type {} which is in success events list. Processing as success.", event.getType());
			// TODO in future you can update payment status to success in database.

			String notificationJson = event.getDataObjectDeserializer().getRawJson();
			log.info("Success event: {}", event);
			log.info("Success event raw JSON: notificationJson: {}", notificationJson);

			CheckoutSessionResponse response = jsonUtil.convertJsonToObject(
					notificationJson, CheckoutSessionResponse.class);
			
			if (response != null 
					&& "paid".equalsIgnoreCase(response.getPaymentStatus())) {
				// Success. Call processing service API to update payment status to success in database.
				triggerSuccessfulPayment(response);
				return;
			} 
			
			log.error("Failed to parse Stripe event data into CheckoutSessionResponse. notificationJson: {}", notificationJson);
			
			throw new StripeProviderException(
					ErrorCodeEnum.INVALID_STRIPE_RESPONSE.getErrorCode(),
					ErrorCodeEnum.INVALID_STRIPE_RESPONSE.getErrorMessage(),
					HttpStatus.BAD_REQUEST);

		}

		if (failedEvents.contains(event.getType())) {
			log.info("Received Stripe event of type {} which is in failed events list. Processing as failed.", event.getType());
			triggerFailedPayment(event);
		}
	}

	private void triggerSuccessfulPayment(CheckoutSessionResponse response) {
		log.info("Triggering successful payment logic for Stripe event with checkout session id: {}, payment status: {}", 
				response.getId(), response.getPaymentStatus());
		// TODO Auto-generated method stub
		// HttpServiceEngine.triggerSuccessfulPayment(response);
		
	}

	private void triggerFailedPayment(Event event) {
		log.info("Triggering failed payment logic for Stripe event id: {}, type: {}", 
				event.getId(), event.getType());
		// TODO Auto-generated method stub
		// HttpServiceEngine.triggerFailedPayment(event);

	}

	private Event isSignatureValid(String stripeSignature, String jsonRequest) {
		Event event;
		try {
			event = Webhook.constructEvent(
					jsonRequest, stripeSignature, endpointSecret
					);
			log.info("Stripe event parsed successfully: id={}, type={}", 
					event.getId(), event.getType());
			return event;

		} catch (Exception e) {
			log.error("Stripe signature verification failed: {}", e.getMessage());

			throw new StripeProviderException(
					ErrorCodeEnum.STRIPE_SIGNATURE_VERIFICATION_ERROR.getErrorCode(),
					ErrorCodeEnum.STRIPE_SIGNATURE_VERIFICATION_ERROR.getErrorMessage(),
					HttpStatus.BAD_REQUEST);
		}
	}
}
