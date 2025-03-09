package com.finance.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class FinanceAppUserApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinanceAppUserApplication.class, args);
		System.out.println("User MS Running");
		System.out.println("testing main");
	}

}
