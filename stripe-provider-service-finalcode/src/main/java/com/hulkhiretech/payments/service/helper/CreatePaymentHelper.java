package com.hulkhiretech.payments.service.helper;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.hulkhiretech.payments.constant.Constant;
import com.hulkhiretech.payments.constant.ErrorCodeEnum;
import com.hulkhiretech.payments.exception.StripeProviderException;
import com.hulkhiretech.payments.http.HttpRequest;
import com.hulkhiretech.payments.pojo.CreatePaymentReq;
import com.hulkhiretech.payments.pojo.LineItem;
import com.hulkhiretech.payments.stripe.CheckoutSessionResponse;
import com.hulkhiretech.payments.stripe.StripeError;
import com.hulkhiretech.payments.stripe.StripeErrorResponse;
import com.hulkhiretech.payments.utl.JsonUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreatePaymentHelper {

	@Value("${stripe.api.key}")
	private String stripeApiKey;
	
	@Value("${stripe.create.session.url}")
	private String stripeCreateSessionUrl;
	
	private final JsonUtil jsonUtil;
	

	public HttpRequest prepareStripeCreateSessionRequest(
			CreatePaymentReq createPaymentReq) {
		
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setBasicAuth(stripeApiKey, "");
		httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		// Form body
		MultiValueMap<String, String> formUrlEncodedData = prepareFormUrlEncodedData(
				createPaymentReq);
		log.info("Prepared form URL encoded data for Stripe create-session API: {}", 
				formUrlEncodedData);
		
		HttpRequest httpRequest = new HttpRequest();
		httpRequest.setHttpMethod(HttpMethod.POST);
		httpRequest.setUrl(stripeCreateSessionUrl);
		httpRequest.setHttpHeaders(httpHeaders);
		httpRequest.setRequestData(formUrlEncodedData);
		
		log.info("Prepared HttpRequest for Stripe create-session API: {}", httpRequest);
		return httpRequest;
	}
	
	public static MultiValueMap<String, String> prepareFormUrlEncodedData(CreatePaymentReq request) {

        MultiValueMap<String, String> formUrlEncodedData = new LinkedMultiValueMap<>();

        // Mandatory fields
        formUrlEncodedData.add(Constant.CREATE_SESSION_MODE, 
        			Constant.CREATE_SESSION_MODE_PAYMENT);
        
        formUrlEncodedData.add(Constant.CREATE_SESSION_SUCCESS_URL, 
        		request.getSuccessUrl());
        
        formUrlEncodedData.add(Constant.CREATE_SESSION_CANCEL_URL, 
        		request.getCancelUrl());

        // Line items
        if (request.getLineItems() != null && !request.getLineItems().isEmpty()) {

            for (int i = 0; i < request.getLineItems().size(); i++) {

                LineItem item = request.getLineItems().get(i);

                String baseKey = Constant.LINE_ITEMS + Constant.OPEN_BRACKET + i + Constant.CLOSE_BRACKET;

                formUrlEncodedData.add(baseKey + Constant.OPEN_BRACKET + Constant.QUANTITY + Constant.CLOSE_BRACKET, String.valueOf(item.getQuantity()));
                formUrlEncodedData.add(baseKey + Constant.OPEN_BRACKET + Constant.PRICE_DATA + Constant.CLOSE_BRACKET + Constant.OPEN_BRACKET + Constant.CURRENCY + Constant.CLOSE_BRACKET, item.getCurrency());
                formUrlEncodedData.add(baseKey + Constant.OPEN_BRACKET + Constant.PRICE_DATA + Constant.CLOSE_BRACKET + Constant.OPEN_BRACKET + Constant.UNIT_AMOUNT + Constant.CLOSE_BRACKET, String.valueOf(item.getUnitAmount()));
                formUrlEncodedData.add(baseKey + Constant.OPEN_BRACKET + Constant.PRICE_DATA + Constant.CLOSE_BRACKET + Constant.OPEN_BRACKET + Constant.PRODUCT_DATA + Constant.CLOSE_BRACKET + Constant.OPEN_BRACKET + Constant.NAME + Constant.CLOSE_BRACKET, item.getProductName());
            }
        }

        return formUrlEncodedData;
    }
	
	public CheckoutSessionResponse processStripeResponse(
			ResponseEntity<String> httpResponse) {

		// Check if httpResponse is 2xx, then convert to CheckoutSessionResponse
		if (httpResponse.getStatusCode().is2xxSuccessful()) {
			log.info("Stripe API call successful. Status code: {}, Response body: {}", 
					httpResponse.getStatusCode(), httpResponse.getBody());

			CheckoutSessionResponse checkoutSession = jsonUtil.convertJsonToObject(
					httpResponse.getBody(), CheckoutSessionResponse.class);
			log.info("Converted CheckoutSessionResponse: {}", checkoutSession);

			if (checkoutSession != null 
					&& checkoutSession.getUrl() != null) {
				log.info("Stripe checkout session created successfully. Session ID: {}, Hosted Page URL: {}", 
						checkoutSession.getId(), checkoutSession.getUrl());

				// SUCCESS
				return checkoutSession;
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
			StripeErrorResponse stripeError = jsonUtil.convertJsonToObject(
					httpResponse.getBody(), StripeErrorResponse.class);
			
			if (stripeError != null && stripeError.getError() != null) {
				log.error("Stripe API error details: Type: {}, Code: {}, Message: {}", 
						stripeError.getError().getType(), 
						stripeError.getError().getCode(), 
						stripeError.getError().getMessage());
				
				
				String stripeConcatinatedErrorMessage = prepareStripeErrorMessage(stripeError);
				log.error("Prepared Stripe error message: {}", stripeConcatinatedErrorMessage);
				
				throw new StripeProviderException(
						ErrorCodeEnum.STRIPE_API_ERROR.getErrorCode(),// DONE
						stripeConcatinatedErrorMessage,// DONE
						HttpStatus.valueOf(httpResponse.getStatusCode().value()));// DONE
			}
			
			log.error("Stripe API call failed with non-JSON error response. Status code: {}, Response body: {}", 
					httpResponse.getStatusCode(), httpResponse.getBody());
		}
		
		
		
		// success object conversion failed
		// no url then also 
		// unable to parse error response body to StripeErrorResponse object

		throw new StripeProviderException(
				ErrorCodeEnum.INVALID_STRIPE_RESPONSE.getErrorCode(),
				ErrorCodeEnum.INVALID_STRIPE_RESPONSE.getErrorMessage(),
				HttpStatus.BAD_GATEWAY);// since stripe gave incorrect response, we can consider it as bad gateway. Its not our fault, its stripe's fault.
	}
	
	private String prepareStripeErrorMessage(StripeErrorResponse stripeErrorResponse) {

	    StripeError error = stripeErrorResponse.getError();

	    return Stream.of(
	                error.getType(),      // always present
	                error.getMessage(),
	                error.getParam(),
	                error.getCode()
	            )
	            .filter(Objects::nonNull)
	            .map(String::trim)
	            .filter(s -> !s.isEmpty())
	            .collect(Collectors.joining(" | "));
	}

}