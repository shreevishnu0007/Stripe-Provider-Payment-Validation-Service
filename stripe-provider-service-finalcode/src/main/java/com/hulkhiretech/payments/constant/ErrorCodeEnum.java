package com.hulkhiretech.payments.constant;

public enum ErrorCodeEnum {

	GENERIC_ERROR("30000", "An unexpected error occurred. Please try again later."),

	// Request-level errors
	CREATE_PAYMENT_REQ_NULL("30001", "CreatePaymentReq is null"),
	SUCCESS_URL_MISSING("30002", "Success URL is missing in createPaymentReq"),
	CANCEL_URL_MISSING("30003", "Cancel URL is missing in createPaymentReq"),
	LINE_ITEMS_MISSING("30004", "Line items are missing in createPaymentReq"),

	// Line item errors
	LINE_ITEM_NULL("30005", "Line item is null"),
	CURRENCY_MISSING("30006", "Currency is missing for line item"),
	PRODUCT_NAME_MISSING("30007", "Product name is missing for line item"),
	UNIT_AMOUNT_INVALID("30008", "Unit amount must be greater than 0 for line item"),
	QUANTITY_INVALID("30009", "Quantity must be greater than 0 for line item"),

	// URL format errors
	SUCCESS_URL_INVALID("30010", "Success URL is not a valid http/https URL"),
	CANCEL_URL_INVALID("30011", "Cancel URL is not a valid http/https URL"),

	ERROR_CONNECTING_TO_EXTERNAL_SERVICE(
			"30012", "Error connecting to external payment service"),
	STRIPE_API_ERROR("30013", "<Dynamically prepare based on stripe error response>"),
	INVALID_STRIPE_RESPONSE("30014", "Received invalid response from Stripe API"),
	MISSING_STRIPE_SIGNATURE_OR_EMPTY_REQUEST(
			"30015", "Missing Stripe-Signature header or empty request body"),
	STRIPE_SIGNATURE_VERIFICATION_ERROR(
			"30016", "Stripe signature verification failed");

	private final String errorCode;
	private final String errorMessage;

	ErrorCodeEnum(String errorCode, String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
}
