package com.moneyfi.monitor;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAdminServer
public class MoneyfiMonitorApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoneyfiMonitorApplication.class, args);
	}

}
