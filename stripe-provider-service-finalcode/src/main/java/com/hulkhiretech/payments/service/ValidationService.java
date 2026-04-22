package com.hulkhiretech.payments.service;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.hulkhiretech.payments.constant.ErrorCodeEnum;
import com.hulkhiretech.payments.exception.StripeProviderException;
import com.hulkhiretech.payments.pojo.CreatePaymentReq;
import com.hulkhiretech.payments.pojo.LineItem;

@Service
public class ValidationService {

    /**
     * Validate CreatePaymentReq and its LineItem entries.
     * Throws StripeProviderException with specific error codes on validation failure.
     */
    public void isValid(CreatePaymentReq req) {
        if (req == null) {
            throw new StripeProviderException(
                    ErrorCodeEnum.CREATE_PAYMENT_REQ_NULL.getErrorCode(),
                    ErrorCodeEnum.CREATE_PAYMENT_REQ_NULL.getErrorMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }

        if (req.getSuccessUrl() == null || req.getSuccessUrl().trim().isEmpty()) {
            throw new StripeProviderException(
                    ErrorCodeEnum.SUCCESS_URL_MISSING.getErrorCode(),
                    ErrorCodeEnum.SUCCESS_URL_MISSING.getErrorMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }

        if (req.getCancelUrl() == null || req.getCancelUrl().trim().isEmpty()) {
            throw new StripeProviderException(
                    ErrorCodeEnum.CANCEL_URL_MISSING.getErrorCode(),
                    ErrorCodeEnum.CANCEL_URL_MISSING.getErrorMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }

        // Validate URL format for successUrl
        if (!isValidHttpUrl(req.getSuccessUrl())) {
            throw new StripeProviderException(
                    ErrorCodeEnum.SUCCESS_URL_INVALID.getErrorCode(),
                    ErrorCodeEnum.SUCCESS_URL_INVALID.getErrorMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }

        // Validate URL format for cancelUrl
        if (!isValidHttpUrl(req.getCancelUrl())) {
            throw new StripeProviderException(
                    ErrorCodeEnum.CANCEL_URL_INVALID.getErrorCode(),
                    ErrorCodeEnum.CANCEL_URL_INVALID.getErrorMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }

        List<LineItem> items = req.getLineItems();
        if (items == null || items.isEmpty()) {
            throw new StripeProviderException(
                    ErrorCodeEnum.LINE_ITEMS_MISSING.getErrorCode(),
                    ErrorCodeEnum.LINE_ITEMS_MISSING.getErrorMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }

        int index = 0;
        for (LineItem item : items) {
            index++;
            if (item == null) {
                throw new StripeProviderException(
                        ErrorCodeEnum.LINE_ITEM_NULL.getErrorCode(),
                        ErrorCodeEnum.LINE_ITEM_NULL.getErrorMessage() + " at index " + (index - 1),
                        HttpStatus.BAD_REQUEST
                );
            }
            if (item.getCurrency() == null || item.getCurrency().trim().isEmpty()) {
                throw new StripeProviderException(
                        ErrorCodeEnum.CURRENCY_MISSING.getErrorCode(),
                        ErrorCodeEnum.CURRENCY_MISSING.getErrorMessage() + " at index " + (index - 1),
                        HttpStatus.BAD_REQUEST
                );
            }
            if (item.getProductName() == null || item.getProductName().trim().isEmpty()) {
                throw new StripeProviderException(
                        ErrorCodeEnum.PRODUCT_NAME_MISSING.getErrorCode(),
                        ErrorCodeEnum.PRODUCT_NAME_MISSING.getErrorMessage() + " at index " + (index - 1),
                        HttpStatus.BAD_REQUEST
                );
            }
            if (item.getUnitAmount() <= 0) {
                throw new StripeProviderException(
                        ErrorCodeEnum.UNIT_AMOUNT_INVALID.getErrorCode(),
                        ErrorCodeEnum.UNIT_AMOUNT_INVALID.getErrorMessage() + " at index " + (index - 1),
                        HttpStatus.BAD_REQUEST
                );
            }
            if (item.getQuantity() <= 0) {
                throw new StripeProviderException(
                        ErrorCodeEnum.QUANTITY_INVALID.getErrorCode(),
                        ErrorCodeEnum.QUANTITY_INVALID.getErrorMessage() + " at index " + (index - 1),
                        HttpStatus.BAD_REQUEST
                );
            }
        }
    }

    // Helper: ensure URL is a valid http or https URL with a host
    private boolean isValidHttpUrl(String url) {
        if (url == null) return false;
        try {
            URI uri = new URI(url.trim());
            String scheme = uri.getScheme();
            String host = uri.getHost();
            return scheme != null && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))
                    && host != null && !host.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}
