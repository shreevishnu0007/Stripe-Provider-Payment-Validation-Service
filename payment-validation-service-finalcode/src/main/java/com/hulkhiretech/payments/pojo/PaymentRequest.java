package com.hulkhiretech.payments.pojo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequest {

    @NotNull(message = "USER_NULL")
    @Valid
    private User user;

    @NotNull(message = "PAYMENT_NULL")
    @Valid
    private Payment payment;

}