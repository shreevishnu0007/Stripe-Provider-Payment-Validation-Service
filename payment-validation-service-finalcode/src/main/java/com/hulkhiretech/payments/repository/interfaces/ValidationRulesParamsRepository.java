package com.hulkhiretech.payments.repository.interfaces;

import java.util.Map;

/**
 * Repository interface for loading validator rule parameters.
 */
public interface ValidationRulesParamsRepository {

    /**
     * Load the parameters for all validators.
     *
     * @return map where key is validatorName and value is a map of paramName->paramValue
     */
    Map<String, Map<String, String>> loadAllValidatorParams();

}
