package com.moneyfi.apigateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@Slf4j
public class MoneyFiApiGatewayApplication {
	public static void main(String[] args) {
		SpringApplication.run(MoneyFiApiGatewayApplication.class, args);
		log.info("API Gateway Service Started");
	}
}
