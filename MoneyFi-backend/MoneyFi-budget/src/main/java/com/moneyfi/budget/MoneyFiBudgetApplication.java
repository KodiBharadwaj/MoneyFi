package com.moneyfi.budget;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MoneyFiBudgetApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoneyFiBudgetApplication.class, args);
		System.out.println("Budget MS Running");
	}

}
