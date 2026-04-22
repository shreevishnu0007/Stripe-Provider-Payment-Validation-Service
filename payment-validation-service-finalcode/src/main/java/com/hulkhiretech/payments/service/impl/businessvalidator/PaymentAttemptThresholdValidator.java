package com.hulkhiretech.payments.service.impl.businessvalidator;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.hulkhiretech.payments.cache.ValidatorRuleCacheV2;
import com.hulkhiretech.payments.constant.ErrorCodeEnum;
import com.hulkhiretech.payments.constant.ValidatorRuleEnum;
import com.hulkhiretech.payments.exception.PaymentValidationException;
import com.hulkhiretech.payments.pojo.PaymentRequest;
import com.hulkhiretech.payments.repository.interfaces.MerchantPaymentRequestRepository;
import com.hulkhiretech.payments.service.interfaces.BusinessValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentAttemptThresholdValidator implements BusinessValidator {

	private final MerchantPaymentRequestRepository merchantReqRepo;
	
	private final ValidatorRuleCacheV2 validatorRuleCache;

	@Override
	public void validate(PaymentRequest paymentRequest) {
		log.info("Validating payment request for attempt threshold: {}", 
				paymentRequest);
		
		Map<String, String> paramsMap = validatorRuleCache.getValidatorParamsForRule(
				ValidatorRuleEnum.PAYMENT_ATTEMPT_THRESHOLD_RULE.getRuleName());
		
		log.debug("Loaded parameters for {}: {}",
				ValidatorRuleEnum.PAYMENT_ATTEMPT_THRESHOLD_RULE.getRuleName(),
				paramsMap);

		int durationInMins = Integer.parseInt(paramsMap.get("durationInMins"));
		int maxPaymentThreshold = Integer.parseInt(paramsMap.get("maxPaymentThreshold"));

		int count = merchantReqRepo.countRequestsForUserInLastMinutes(
				paymentRequest.getUser().getEndUserID(),
				durationInMins);

		log.info("Count of payment attempts for user {} in last {} minutes: {}",
				paymentRequest.getUser().getEndUserID(),
				durationInMins,
				count);

		if(count <= maxPaymentThreshold) {// allow the payment
			log.info("Payment request is valid, attempt count {} is within threshold {}",
					count, maxPaymentThreshold);

			return;
		}

		log.error("Payment request exceeds attempt threshold. "
				+ "Attempt count: {}, Threshold: {}",
				count, maxPaymentThreshold);
		
		throw new PaymentValidationException(
				ErrorCodeEnum.PAYMENT_ATTEMPT_THRESHOLD_EXCEEDED.getErrorCode(),
				ErrorCodeEnum.PAYMENT_ATTEMPT_THRESHOLD_EXCEEDED.getErrorMessage(),
				HttpStatus.TOO_MANY_REQUESTS);
	}

}
