package com.hulkhiretech.payments.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class PaymentValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private String errorCode;
    private String errorMessage;
    private HttpStatus httpStatus;

    public PaymentValidationException(
    		String errorCode, 
    		String errorMessage, 
    		HttpStatus httpStatus) {
    	
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.httpStatus = httpStatus;
    }


}
