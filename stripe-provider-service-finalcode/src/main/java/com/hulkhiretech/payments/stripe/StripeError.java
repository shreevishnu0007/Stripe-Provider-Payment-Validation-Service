package com.hulkhiretech.payments.stripe;

import lombok.Data;

@Data
public class StripeError {
	
	private String code;
    private String message;
    private String param;
    private String type;

}
