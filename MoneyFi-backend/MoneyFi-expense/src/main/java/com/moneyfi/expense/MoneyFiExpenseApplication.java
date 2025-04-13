package com.moneyfi.expense;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MoneyFiExpenseApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoneyFiExpenseApplication.class, args);
		System.out.println("Expense MS Running");
	}

}
