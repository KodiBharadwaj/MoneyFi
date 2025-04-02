package com.moneyfi.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class MoneyFiUserApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoneyFiUserApplication.class, args);
		System.out.println("User MS Running");
	}

}
