package com.moneyfi.user.config.database;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class PrimaryMsSqlDbConfig {

    @Primary
    @Bean(name = {"transactionManager", "sqlServerTransactionManager"})
    public PlatformTransactionManager sqlServerTransactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
