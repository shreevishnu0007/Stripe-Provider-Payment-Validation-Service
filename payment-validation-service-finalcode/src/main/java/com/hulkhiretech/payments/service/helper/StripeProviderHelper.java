package com.hulkhiretech.payments.service.helper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hulkhiretech.payments.constant.ErrorCodeEnum;
import com.hulkhiretech.payments.exception.PaymentValidationException;
import com.hulkhiretech.payments.http.HttpRequest;
import com.hulkhiretech.payments.pojo.PaymentRequest;
import com.hulkhiretech.payments.stripeprovider.LineItem;
import com.hulkhiretech.payments.stripeprovider.SPCreatePaymentReq;
import com.hulkhiretech.payments.stripeprovider.SPErrorResponse;
import com.hulkhiretech.payments.stripeprovider.SPPaymentResponse;
import com.hulkhiretech.payments.util.JsonUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class StripeProviderHelper {
	
	private final JsonUtil jsonUtil;
	
	@Value("${stripe.provider.createPaymentUrl}")
	private String createStripeProviderPaymentUrl; 

	public HttpRequest createHttpRequest(PaymentRequest paymentRequest) {
		log.info("Creating HttpRequest from PaymentRequest: {}", paymentRequest);

		SPCreatePaymentReq spReq = new SPCreatePaymentReq();
		// Map simple fields
		spReq.setSuccessUrl(paymentRequest.getPayment().getSuccessUrl());
		spReq.setCancelUrl(paymentRequest.getPayment().getCancelUrl());

		// Map line items if present
		if (paymentRequest.getPayment().getLineItems() != null 
				&& !paymentRequest.getPayment().getLineItems().isEmpty()) {
			List<LineItem> spLineItems = paymentRequest.getPayment()
				.getLineItems()
				.stream()
				.map(li -> {
					LineItem item = new LineItem();
					item.setCurrency(li.getCurrency());
					item.setProductName(li.getProductName());
					// handle possible nulls for Integer -> int
					item.setUnitAmount(
							li.getUnitAmount() == null ? 0 : li.getUnitAmount());
					item.setQuantity(
							li.getQuantity() == null ? 0 : li.getQuantity());
					return item;
				})
				.collect(Collectors.toList());

			spReq.setLineItems(spLineItems);
		}

		HttpRequest httpRequest = new HttpRequest();
		httpRequest.setHttpHeaders(new HttpHeaders());
		httpRequest.setHttpMethod(HttpMethod.POST);
		httpRequest.setUrl(createStripeProviderPaymentUrl);
		httpRequest.setRequestData(spReq);

		log.debug("Prepared HttpRequest: {}", httpRequest);
		return httpRequest;
	}

	public SPPaymentResponse processResponse(ResponseEntity<String> httpResponse) {

		// Check if httpResponse is 2xx, then convert to CheckoutSessionResponse
		if (httpResponse.getStatusCode().is2xxSuccessful()) {
			log.info("Stripe API call successful. Status code: {}, Response body: {}", 
					httpResponse.getStatusCode(), httpResponse.getBody());

			SPPaymentResponse paymentResponse = jsonUtil.convertJsonToObject(
					httpResponse.getBody(), SPPaymentResponse.class);
			log.info("Converted CheckoutSessionResponse: {}", paymentResponse);

			if (paymentResponse != null 
					&& paymentResponse.getHostedPageUrl() != null) {
				log.info("Stripe API call successful and valid response received. Hosted page URL: {}", 
						paymentResponse.getHostedPageUrl());

				// SUCCESS
				return paymentResponse;
			} 
			
			log.error("Stripe API call returned 2xx but response body is invalid. Status code: {}, Response body: {}", 
					httpResponse.getStatusCode(), httpResponse.getBody());
		}
		
		// if code comes here, means its a failed/error
		// 4xx 5xx
		if (httpResponse.getStatusCode().is4xxClientError() 
				|| httpResponse.getStatusCode().is5xxServerError()) {
			log.error("Stripe API call failed. Status code: {}, Response body: {}", 
					httpResponse.getStatusCode(), httpResponse.getBody());
			
			
			// use jsonutil to convert the error response body to StripeErrorResponse object and log the error details.
			SPErrorResponse stripeError = jsonUtil.convertJsonToObject(
					httpResponse.getBody(), SPErrorResponse.class);
			
			if (stripeError != null) {
				log.error("Stripe API call failed with error response. Status code: {}, Stripe error code: {}, Stripe error message: {}", 
						httpResponse.getStatusCode(), stripeError.getErrorCode(), stripeError.getErrorMessage());
				
				throw new PaymentValidationException(
						stripeError.getErrorCode(),
						stripeError.getErrorMessage(),// DONE
						HttpStatus.valueOf(httpResponse.getStatusCode().value()));// DONE
			}
			
			log.error("Stripe API call failed with non-JSON error response. Status code: {}, Response body: {}", 
					httpResponse.getStatusCode(), httpResponse.getBody());
		}
		
		throw new PaymentValidationException(
				ErrorCodeEnum.INVALID_STRIPE_PROVIDER_RESPONSE.getErrorCode(),
				ErrorCodeEnum.INVALID_STRIPE_PROVIDER_RESPONSE.getErrorMessage(),
				HttpStatus.BAD_GATEWAY);// since stripe gave incorrect response, we can consider it as bad gateway. Its not our fault, its stripe's fault.
	}
}