package com.moneyfi.wealthcore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MoneyFiWealthCoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoneyFiWealthCoreApplication.class, args);
		System.out.println("Budget MS Running");
	}

}
