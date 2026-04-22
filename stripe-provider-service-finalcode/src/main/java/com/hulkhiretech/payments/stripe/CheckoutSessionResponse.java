package com.hulkhiretech.payments.stripe;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CheckoutSessionResponse {

    private String id;

    private String url;

    private String status;

    @JsonProperty("payment_status")
    private String paymentStatus;
}