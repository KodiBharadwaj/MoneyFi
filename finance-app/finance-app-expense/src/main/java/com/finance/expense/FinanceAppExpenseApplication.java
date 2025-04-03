package com.finance.expense;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class FinanceAppExpenseApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinanceAppExpenseApplication.class, args);
		System.out.println("Expense MS Running");
	}

}
