package com.finance.goal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class FinanceAppGoalApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinanceAppGoalApplication.class, args);
		System.out.println("Goal MS Running");
	}

}
