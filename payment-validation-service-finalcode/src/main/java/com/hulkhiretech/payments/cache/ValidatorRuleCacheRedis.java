package com.hulkhiretech.payments.cache;

import java.util.List;
import java.util.Map;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import com.hulkhiretech.payments.repository.interfaces.ValidationRulesParamsRepository;
import com.hulkhiretech.payments.repository.interfaces.ValidationRulesRepository;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

//@Component // Commenting this in order to test in local without Redis.
@Slf4j
public class ValidatorRuleCacheRedis {

	//private List<String> validatorRules;

	private static final String VALIDATOR_RULE_PARAMS_PREFIX = "validator-rule-params:";

	private static final String VALIDATOR_RULES_KEY = "validator-rules";

	//private Map<String, Map<String, String>> validatorRuleParams;

	// repositories to load rules and params from DB
	private final ValidationRulesRepository validationRulesRepository;

	private final ValidationRulesParamsRepository validationRulesParamsRepository;

	private final RedisTemplate<String, String> redisTemplate;

	private ListOperations<String, String> listOperations;
	private HashOperations<String, String, String> hashOperations;

	public ValidatorRuleCacheRedis(
			ValidationRulesRepository validationRulesRepository,
			ValidationRulesParamsRepository validationRulesParamsRepository,
			RedisTemplate<String, String> redisTemplate) {

		this.validationRulesRepository = validationRulesRepository;
		this.validationRulesParamsRepository = validationRulesParamsRepository;
		this.redisTemplate = redisTemplate;
		this.listOperations = redisTemplate.opsForList();
		this.hashOperations = redisTemplate.opsForHash();
	}

	public List<String> getValidatorRules() {
		// read list values using listOperations for redis key "validator-rules"
		return listOperations.range(VALIDATOR_RULES_KEY, 0, -1);
	}
	
	/**
	 * get ValidatorRule params for given rule name. 
	 * Read from redis hash with key "validator-rule-params:{validatorName}" 
	 * return Map<String, String> of paramName and paramValue
	 */
	public Map<String, String> getValidatorParamsForRule(String ruleName) {
		String redisKey = VALIDATOR_RULE_PARAMS_PREFIX + ruleName;
		return hashOperations.entries(redisKey);
	}

	/**
	 *  Write method to set validator rules in redis cache take List<String>
	 *  as input and store in redis list VALIDATOR_RULES_KEY
	 */
	public void setValidatorRules(List<String> rules) {  
		// delete existing list in redis
		redisTemplate.delete(VALIDATOR_RULES_KEY);

		if (rules != null && !rules.isEmpty()) {
			// write list values using listOperations for redis key "validator-rules"
			listOperations.rightPushAll(VALIDATOR_RULES_KEY, rules);
			log.info("Updated validator rules in Redis cache: {}", rules);
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

		// has valid data
		for (Map.Entry<String, Map<String, String>> entry : validatorRuleParams.entrySet()) {
			String validatorName = entry.getKey();
			Map<String, String> params = entry.getValue();
			String redisKey = VALIDATOR_RULE_PARAMS_PREFIX + validatorName;

			// delete existing hash for this validator
			redisTemplate.delete(redisKey);

			if (params != null && !params.isEmpty()) {
				hashOperations.putAll(redisKey, params);
				log.info("Updated params for validator {} in Redis cache: {}", 
						validatorName, params);
			} else {
				log.info("No params to update for validator {} in Redis cache; cleared existing params", 
						validatorName);
			}
		}

	}

	/*
	public Map<String, String> getValidatorParamsForRule(String ruleName) {
		return validatorRuleParams.getOrDefault(ruleName, Map.of());
	}
	 */

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
