package com.hulkhiretech.payments;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class PaymentConfigServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentConfigServerApplication.class, args);
	}

}
