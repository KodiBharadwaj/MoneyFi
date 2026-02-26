package com.moneyfi.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MoneyfiUserApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoneyfiUserApplication.class, args);
		System.out.println("RABBIT URI: " + System.getenv("RABBITMQ_URI"));

		System.out.println("User service is running");
	}

}
