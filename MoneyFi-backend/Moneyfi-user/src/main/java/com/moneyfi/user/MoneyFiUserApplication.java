package com.moneyfi.user;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.ZonedDateTime;
import java.util.TimeZone;

@Slf4j
@EnableCaching
@EnableScheduling
@SpringBootApplication
public class MoneyFiUserApplication {

    @Value("${application.time-zone}")
    private String applicationTimezone;

    @PostConstruct
    private void init() {
        TimeZone.setDefault(TimeZone.getTimeZone(applicationTimezone));
        log.info("Application timezone is {}. Today = {} ", TimeZone.getDefault().toZoneId(), ZonedDateTime.now());
        log.info("Application started as user: {}", System.getProperty("user.name"));
    }

	public static void main(String[] args) {
		SpringApplication.run(MoneyFiUserApplication.class, args);
		log.info("User Service Started");
	}
}
