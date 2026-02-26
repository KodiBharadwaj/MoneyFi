package com.moneyfi.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;

@SpringBootApplication
@Slf4j
@EnableJms
public class MoneyFiNotificationApplication {
	public static void main(String[] args) {
		SpringApplication.run(MoneyFiNotificationApplication.class, args);
		log.info("Notification Service Started");
	}
}
