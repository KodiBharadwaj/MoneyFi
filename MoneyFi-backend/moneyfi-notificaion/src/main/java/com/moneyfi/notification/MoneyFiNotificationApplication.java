package com.moneyfi.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class MoneyFiNotificationApplication {
	public static void main(String[] args) {
		SpringApplication.run(MoneyFiNotificationApplication.class, args);
		System.out.println("RABBIT URI: " + System.getenv("RABBITMQ_URI"));
		log.info("Notification Service Started");
	}
}
