package com.hulkhiretech.payments.constant;

import java.util.Optional;

import com.hulkhiretech.payments.service.impl.businessvalidator.DuplicateTxnValidator;
import com.hulkhiretech.payments.service.impl.businessvalidator.PaymentAttemptThresholdValidator;
import com.hulkhiretech.payments.service.interfaces.BusinessValidator;

/**
 * Enum of validator rules mapping a rule name to its BusinessValidator implementing class.
 */
public enum ValidatorRuleEnum {
    DUPLICATE_TXN_RULE(
    		"DUPLICATE_TXN_RULE", DuplicateTxnValidator.class),
    PAYMENT_ATTEMPT_THRESHOLD_RULE(
    		"PAYMENT_ATTEMPT_THRESHOLD_RULE", PaymentAttemptThresholdValidator.class)
    ;

    private final String ruleName;
    private final Class<? extends BusinessValidator> validatorClass;

    ValidatorRuleEnum(String ruleName, Class<? extends BusinessValidator> validatorClass) {
        this.ruleName = ruleName;
        this.validatorClass = validatorClass;
    }

    public String getRuleName() {
        return ruleName;
    }

    public Class<? extends BusinessValidator> getValidatorClass() {
        return validatorClass;
    }

    /**
     * Find the validator class for the given rule name (case-sensitive).
     * Returns Optional.empty() if no match.
     */
    public static Optional<Class<? extends BusinessValidator>> getValidatorClassByRule(String ruleName) {
        if (ruleName == null) {
            return Optional.empty();
        }
        for (ValidatorRuleEnum v : values()) {
            if (v.ruleName.equals(ruleName)) {
                return Optional.of(v.getValidatorClass());
            }
        }
        return Optional.empty();
    }
}