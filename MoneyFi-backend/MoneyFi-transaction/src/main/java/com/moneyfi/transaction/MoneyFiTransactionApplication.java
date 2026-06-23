package com.moneyfi.transaction;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.ZonedDateTime;
import java.util.TimeZone;

@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@Slf4j
public class MoneyFiTransactionApplication {

    @Value("${application.time-zone}")
    private String applicationTimezone;

    @PostConstruct
    private void init() {
        TimeZone.setDefault(TimeZone.getTimeZone(applicationTimezone));
        log.info("Application timezone is {}. Today = {} ", TimeZone.getDefault().toZoneId(), ZonedDateTime.now());
        log.info("Application started as user: {}", System.getProperty("user.name"));
    }

	public static void main(String[] args) {
		SpringApplication.run(MoneyFiTransactionApplication.class, args);
		log.info("Transaction Service Started");
	}
}
