package com.hulkhiretech.payments.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.hulkhiretech.payments.constant.Constant;
import com.hulkhiretech.payments.constant.ErrorCodeEnum;
import com.hulkhiretech.payments.exception.PaymentValidationException;
import com.hulkhiretech.payments.service.HmacSha256Service;
import com.hulkhiretech.payments.util.JsonUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class HmacSha256Filter extends OncePerRequestFilter {

	private final HmacSha256Service hmacSha256Service;
	private final JsonUtil jsonUtil;

	@Override
	protected void doFilterInternal(
			HttpServletRequest request, HttpServletResponse response, 
			FilterChain filterChain)
					throws ServletException, IOException {
		log.info("HmacSha256Filter: Processing request for URI: {}", request.getRequestURI());

		log.info("HmacSha256Filter: Reading Hmac-Signature header from request...hmacSha256Service: {}"
				, hmacSha256Service);
		
		// TODO include HmacSha256Service call here.
		// read Hmac-Signature header from request.
		String hmacSignature = request.getHeader("Hmac-Signature");

		// give json

		WrappedRequest wrappedRequest = new WrappedRequest(request);

		String jsonBody = wrappedRequest.getBody();

		log.info("Read from wrappedRequest jsonBody: {}", jsonBody);

		String formatedJson = null;
		try {
			formatedJson = jsonUtil.prepareFormattedJson(jsonBody);
		} catch (Exception e) {
			log.error("Error while formatting JSON body: {}", e.getMessage());
			throw new PaymentValidationException(
					ErrorCodeEnum.INVALID_JSON.getErrorCode(), 
					ErrorCodeEnum.INVALID_JSON.getErrorMessage(), 
					HttpStatus.BAD_REQUEST);
		}

		hmacSha256Service.isHmacSignatureValid(formatedJson, hmacSignature);

		log.info("HmacSha256Filter: HMAC validation passed for URI: {}", request.getRequestURI());

		// below runs only for success case. 
		// Incase there is HmacFailure above method will throw exception and request will not reach here.
		
		
		// here we know request is authenticated.
		// we need to inform spring security that request is authenticated.

		SecurityContext context = SecurityContextHolder.createEmptyContext(); 
		Authentication authentication =
				new HmacAuthenticationToken(
						Constant.MERCHANT_ID, 
						hmacSignature, 
						Constant.ROLE_MERCHANT);

		context.setAuthentication(authentication);

		SecurityContextHolder.setContext(context);

		filterChain.doFilter(wrappedRequest, response); 

		log.info("HmacSha256Filter: Finished processing request for URI: {}", request.getRequestURI());

	}

}
