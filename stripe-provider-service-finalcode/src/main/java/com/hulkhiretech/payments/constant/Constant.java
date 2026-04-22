package com.hulkhiretech.payments.constant;

public class Constant {
	private Constant() {
		// Private constructor to prevent instantiation
	}
	
	public static final String CREATE_SESSION_SUCCESS_URL = "success_url";

	public static final String CREATE_SESSION_MODE = "mode";
	
	public static final String CREATE_SESSION_MODE_PAYMENT = "payment";

	public static final String CREATE_SESSION_CANCEL_URL = "cancel_url";

	// Keys used when building Stripe form-urlencoded line items
	public static final String LINE_ITEMS = "line_items";
	public static final String QUANTITY = "quantity";
	public static final String PRICE_DATA = "price_data";
	public static final String CURRENCY = "currency";
	public static final String UNIT_AMOUNT = "unit_amount";
	public static final String PRODUCT_DATA = "product_data";
	public static final String NAME = "name";

	// Helpers to build bracketed keys
	public static final String OPEN_BRACKET = "[";
	public static final String CLOSE_BRACKET = "]";

}