package com.finance.income;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class FinanceAppIncomeApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinanceAppIncomeApplication.class, args);
		System.out.println("Income MS Running");
	}

}
