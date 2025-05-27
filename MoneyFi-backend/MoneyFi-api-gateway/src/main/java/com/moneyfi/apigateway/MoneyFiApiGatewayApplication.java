package com.moneyfi.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MoneyFiApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoneyFiApiGatewayApplication.class, args);
		System.out.println("API Gateway running");
	}

}
