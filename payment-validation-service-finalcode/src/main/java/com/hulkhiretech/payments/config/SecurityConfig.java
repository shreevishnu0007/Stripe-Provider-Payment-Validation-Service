package com.hulkhiretech.payments.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import com.hulkhiretech.payments.service.HmacSha256Service;
import com.hulkhiretech.payments.util.JsonUtil;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	
	private final HmacSha256Service hmacSha256Service;
	private final JsonUtil jsonUtil;

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http

		.csrf(csrf -> csrf.disable())

		.authorizeHttpRequests((authorize) -> authorize
				.anyRequest().permitAll()  
				)
		/* TODO, below is commonly only temporary for testing. 
		 * DONT COMMIT 
		 * 
		.authorizeHttpRequests((authorize) -> authorize
				.anyRequest().authenticated()  
				)  
		
		.addFilterBefore(new ExceptionHandlerFilter(jsonUtil), 
				DisableEncodeUrlFilter.class)
		
		.addFilterAfter(new HmacSha256Filter(hmacSha256Service, jsonUtil), 
				LogoutFilter.class) 
		*/
		.sessionManagement(session -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
		;

		return http.build(); 
	}

}
