package com.hulkhiretech.payments.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.hulkhiretech.payments.constant.ErrorCodeEnum;
import com.hulkhiretech.payments.pojo.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(StripeProviderException.class)
    public ResponseEntity<ErrorResponse> handleStripeProviderException(StripeProviderException ex) {
        log.error("StripeProviderException caught: {}", ex.toString());

        HttpStatus status = ex.getHttpStatus();

        ErrorResponse body = new ErrorResponse();
        body.setErrorCode(ex.getErrorCode());
        body.setErrorMessage(ex.getErrorMessage());

        log.error("Returning error response: status={}, body={}", status, body);
        return new ResponseEntity<>(body, status);
    }
    
    /**
     * Handle Exception.class, 
     * return ErrorResponse with Generic error code and message, 
     * and HTTP 500 
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
    			log.error("Generic exception caught: ", ex);

		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

		ErrorResponse body = new ErrorResponse();
		body.setErrorCode(ErrorCodeEnum.GENERIC_ERROR.getErrorCode());
		body.setErrorMessage(ErrorCodeEnum.GENERIC_ERROR.getErrorMessage());

		log.error("Returning generic error response: status={}, body={}", status, body);
		return new ResponseEntity<>(body, status);
    }
}
