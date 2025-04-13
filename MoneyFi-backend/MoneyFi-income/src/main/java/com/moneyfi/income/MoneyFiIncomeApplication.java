package com.moneyfi.income;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MoneyFiIncomeApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoneyFiIncomeApplication.class, args);
		System.out.println("Income MS Running");
	}

}
