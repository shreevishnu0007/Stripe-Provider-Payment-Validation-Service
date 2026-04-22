package com.hulkhiretech.payments.repository.interfaces;

import java.util.List;

/**
 * Repository interface for loading active validator rule names.
 */
public interface ValidationRulesRepository {

    /**
     * Load the list of active validator names ordered by priority ascending.
     *
     * @return list of validator names (e.g. "DUPLICATE_TXN_RULE")
     */
    List<String> loadActiveValidatorNamesOrderedByPriority();

}