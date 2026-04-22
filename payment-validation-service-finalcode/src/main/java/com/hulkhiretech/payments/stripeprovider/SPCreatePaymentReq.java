package com.hulkhiretech.payments.stripeprovider;

import java.util.List;

import lombok.Data;

@Data
public class SPCreatePaymentReq {
	
	private String successUrl;
	
	private String cancelUrl;

	List<LineItem> lineItems;

}