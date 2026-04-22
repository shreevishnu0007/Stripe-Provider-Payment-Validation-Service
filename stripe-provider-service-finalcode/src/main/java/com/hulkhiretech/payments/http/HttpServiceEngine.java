package com.hulkhiretech.payments.http;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import com.hulkhiretech.payments.constant.ErrorCodeEnum;
import com.hulkhiretech.payments.exception.StripeProviderException;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class HttpServiceEngine {

	private final RestClient restClient;

	public ResponseEntity<String> makeHttpCall(HttpRequest httpRequest) {
		log.info("Making HTTP call to external service...");

		try {
			ResponseEntity<String> httpResponse = restClient
					.method(httpRequest.getHttpMethod())
					.uri(httpRequest.getUrl())
					.headers(
							restClientHeaders -> restClientHeaders.addAll(
									httpRequest.getHttpHeaders()))
					.body(httpRequest.getRequestData())
					.retrieve()
					.toEntity(String.class);

			log.info("HTTP call completed. Status code: {}, Response body: {}", 
					httpResponse.getStatusCode(), httpResponse.getBody());

			return httpResponse;
		} catch (HttpClientErrorException | HttpServerErrorException ex) {
			// means got valid error response from stripe. 4xx or 5xx
			
			log.error("HTTP error occurred while making HTTP call: Status code: {}, Response body: {}", 
					ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
			
			// if we get 503 or 504, then throw StripeProviderException
			if (ex.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE || 
					ex.getStatusCode() == HttpStatus.GATEWAY_TIMEOUT) {
				log.error("Stripe service is unavailable. Status code: {}, Response body: {}", 
						ex.getStatusCode(), ex.getResponseBodyAsString());
				
				throw new StripeProviderException(
						ErrorCodeEnum.ERROR_CONNECTING_TO_EXTERNAL_SERVICE.getErrorCode(),
						ErrorCodeEnum.ERROR_CONNECTING_TO_EXTERNAL_SERVICE.getErrorMessage(),
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
			
			
			// prepare ResponseEntity with error details from the exception and return to the caller.
			ResponseEntity<String> errorResponse = ResponseEntity
					.status(ex.getStatusCode())
					.body(ex.getResponseBodyAsString());
			
			return errorResponse; // Let the exception propagate to be handled by GlobalExceptionHandler
		} catch (Exception ex) {
			// when you are not able to get http response from stripe. Network error, timeout, DNS failure, etc.
			
			log.error("Error occurred while making HTTP call: ", ex);
			
			throw new StripeProviderException(
					ErrorCodeEnum.ERROR_CONNECTING_TO_EXTERNAL_SERVICE.getErrorCode(),
					ErrorCodeEnum.ERROR_CONNECTING_TO_EXTERNAL_SERVICE.getErrorMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostConstruct
	public void init() {
		log.info("Initializing HttpServiceEngine... restClient: {}", restClient);
	}

}
