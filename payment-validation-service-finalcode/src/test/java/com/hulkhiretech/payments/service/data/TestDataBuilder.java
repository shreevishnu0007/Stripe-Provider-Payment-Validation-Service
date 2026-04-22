package com.hulkhiretech.payments.service.data;

import java.util.ArrayList;
import java.util.List;

import com.hulkhiretech.payments.pojo.LineItem;
import com.hulkhiretech.payments.pojo.Payment;
import com.hulkhiretech.payments.pojo.PaymentRequest;
import com.hulkhiretech.payments.pojo.User;

public class TestDataBuilder {

    public static PaymentRequest buildPaymentRequest() {

        // ---- User ----
        User user = new User();
        user.setEndUserID("user123123");
        user.setFirstname("John hello");
        user.setLastname("Doe");
        user.setEmail("john.doe@example.com");
        user.setMobilePhone("+1234567890");


        // ---- Line Item 1 ----
        LineItem item1 = new LineItem();
        item1.setCurrency("EUR");
        item1.setProductName("Phon");
        item1.setUnitAmount(200);
        item1.setQuantity(1);


        // ---- Line Item 2 ----
        LineItem item2 = new LineItem();
        item2.setCurrency("EUR");
        item2.setProductName("Headphones");
        item2.setUnitAmount(500);
        item2.setQuantity(2);


        List<LineItem> lineItems = new ArrayList<>();
        lineItems.add(item1);
        lineItems.add(item2);


        // ---- Payment ----
        Payment payment = new Payment();
        payment.setCurrency("USD");
        payment.setAmount(100);
        payment.setBrandName("MyShop");
        payment.setLocale("en-US");
        payment.setCountry("US");
        payment.setMerchantTxnRef("TXN1234560007");
        payment.setPaymentMethod("APM");
        payment.setProvider("STRIPE");
        payment.setPaymentType("SALE");
        payment.setSuccessUrl("https://example.com/success");
        payment.setCancelUrl("https://example.com/cancel");
        payment.setLineItems(lineItems);


        // ---- PaymentRequest ----
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setUser(user);
        paymentRequest.setPayment(payment);

        return paymentRequest;
    }
}