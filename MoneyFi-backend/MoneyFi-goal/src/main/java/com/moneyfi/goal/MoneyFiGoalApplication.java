package com.moneyfi.goal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MoneyFiGoalApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoneyFiGoalApplication.class, args);
		System.out.println("Goal MS Running");
	}

}
