package com.hulkhiretech.payments.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Individual item to be charged in the checkout session.")
public class LineItem {

	@Schema(description = "ISO currency code for the item (lowercase preferred, e.g., 'usd').", example = "usd")
	private String currency;

	@Schema(description = "Human-readable product name shown on the checkout page.", example = "T-shirt (blue)")
	private String productName;

	@Schema(description = "Unit amount in the smallest currency unit (e.g., cents). Must be > 0.", example = "1999")
	private int unitAmount;

	@Schema(description = "Quantity of this item to charge. Must be > 0.", example = "1")
	private int quantity;
}