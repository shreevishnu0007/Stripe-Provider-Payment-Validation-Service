package com.hulkhiretech.payments.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.hulkhiretech.payments.repository.interfaces.ValidationRulesParamsRepository;
import com.hulkhiretech.payments.repository.interfaces.ValidationRulesRepository;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ValidatorRuleCacheV2 { // Created this v2 only for local testing without Redis. We can replace existing ValidatorRuleCache with this in future after testing.

	private List<String> validatorRules = new ArrayList<>(); 
	private Map<String, Map<String, String>> validatorRuleParams = new HashMap<>();

	// repositories to load rules and params from DB
	private final ValidationRulesRepository validationRulesRepository;

	private final ValidationRulesParamsRepository validationRulesParamsRepository;

	public ValidatorRuleCacheV2(
			ValidationRulesRepository validationRulesRepository,
			ValidationRulesParamsRepository validationRulesParamsRepository) {

		this.validationRulesRepository = validationRulesRepository;
		this.validationRulesParamsRepository = validationRulesParamsRepository;
	}

	public List<String> getValidatorRules() {
		return validatorRules;
	}
	
	/**
	 *  Write method to set validator rules in redis cache take List<String>
	 *  as input and store in redis list VALIDATOR_RULES_KEY
	 */
	public void setValidatorRules(List<String> rules) {  
		// delete existing list in redis
		validatorRules.clear();

		if (rules != null && !rules.isEmpty()) {
			
			validatorRules = List.copyOf(rules);
			
			log.info("Updated validator rules in validatorRules: {}", validatorRules);
		} else {
			log.info("No validator rules to update in Redis cache; cleared existing rules");
		}
	}

	/**
	 * Write a method which takes Map<String, Map<String, String>> as input
	 * and store in redis hash with key "validator-rule-params:{validatorName}"
	 * and field as paramName and value as paramValue
	 */
	public void setValidatorRuleParams(Map<String, Map<String, String>> validatorRuleParams) {
		// if null or empty then return;
		if (validatorRuleParams == null || validatorRuleParams.isEmpty()) {
			log.info("No validator rule params to update in Redis cache; cleared all existing params");
			return;
		}
		
		this.validatorRuleParams.clear();
		
		this.validatorRuleParams.putAll(validatorRuleParams);
	}

	public Map<String, String> getValidatorParamsForRule(String ruleName) {
		return validatorRuleParams.getOrDefault(ruleName, Map.of());
	}

	// init method with postconstruct and log validator rules
	@PostConstruct
	public void init() { 
		// load list of validator names from DB or fallback to empty list
		try {

			List<String> validatorRules = getValidatorRules(); 

			// if values is validatorRules then return. No need of further DB call.
			if(validatorRules != null && !validatorRules.isEmpty()) {
				log.info("Loaded {} validator rules from Redis cache validatorRules:{}", 
						validatorRules != null ? validatorRules.size() : 0, validatorRules);

				return;
			}

			log.info("No validator rules found in Redis cache; loading from DB");
			List<String> dbRules = validationRulesRepository
					.loadActiveValidatorNamesOrderedByPriority();

			if (dbRules != null && !dbRules.isEmpty()) {
				setValidatorRules(dbRules);
				log.info("Loaded {} validator rules from DB", dbRules.size());
			} else {
				// no DB rules found -> use empty list
				//this.validatorRules = List.of();
				log.info("No validator rules found in DB; using empty rule set");
			}

			// load params from DB
			Map<String, Map<String, String>> validatorRuleParams = validationRulesParamsRepository
					.loadAllValidatorParams();
			if (validatorRuleParams == null) {
				validatorRuleParams = Map.of();
			}
			setValidatorRuleParams(validatorRuleParams);

			log.info("Loaded validator rule config entries: {}", validatorRuleParams.size());

		} catch (Exception ex) {
			log.error("Failed to load validator rules or params from DB; using empty rule set and config", ex);
		}
	}
}
