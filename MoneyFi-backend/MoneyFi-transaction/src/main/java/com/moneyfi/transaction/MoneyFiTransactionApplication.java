package com.moneyfi.transaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MoneyFiTransactionApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoneyFiTransactionApplication.class, args);
		System.out.println("Transaction Service Running");
	}

}
