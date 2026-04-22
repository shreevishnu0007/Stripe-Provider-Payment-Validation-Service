package com.hulkhiretech.payments.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.hulkhiretech.payments.constant.ErrorCodeEnum;
import com.hulkhiretech.payments.pojo.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex) {
		log.error("Validation error occurred: {}", ex.getMessage());

        FieldError fieldError = ex.getBindingResult()
                .getFieldErrors()
                .get(0);  // first error only (optional design choice)

        String enumKey = fieldError.getDefaultMessage();

        ErrorCodeEnum errorCodeEnum = ErrorCodeEnum.valueOf(enumKey);

        ErrorResponse response = new ErrorResponse(
                errorCodeEnum.getErrorCode(),
                errorCodeEnum.getErrorMessage()
        );
        
        log.error("Validation error: {}", response);

        return ResponseEntity.badRequest().body(response);
    }
	
	@ExceptionHandler(PaymentValidationException.class)
    public ResponseEntity<ErrorResponse> handlePaymentValidationException(PaymentValidationException ex) {
        log.error("StripeProviderException caught: {}", ex.toString());

        HttpStatus status = ex.getHttpStatus();

        ErrorResponse body = new ErrorResponse(
        		ex.getErrorCode(), 
        		ex.getErrorMessage());

        log.error("Returning error response: status={}, body={}", status, body);
        return new ResponseEntity<>(body, status);
    }
	
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