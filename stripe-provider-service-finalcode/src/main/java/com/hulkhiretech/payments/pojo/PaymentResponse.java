package com.hulkhiretech.payments.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Response returned after creating a Stripe checkout session. Contains the Stripe session id and the hosted payment page URL.")
public class PaymentResponse {

    @Schema(description = "Stripe Checkout Session ID. Use this id to retrieve session details.", example = "cs_test_a1b2c3d4")
	private String stripeSessionId;

    @Schema(description = "Hosted payment page URL where the user completes the payment.", example = "https://checkout.stripe.com/pay/cs_test_a1b2c3d4")
	private String hostedPageUrl;
}