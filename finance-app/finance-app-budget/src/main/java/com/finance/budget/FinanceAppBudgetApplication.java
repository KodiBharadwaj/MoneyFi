package com.finance.budget;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class FinanceAppBudgetApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinanceAppBudgetApplication.class, args);
		System.out.println("Budget MS Running");
	}

}
