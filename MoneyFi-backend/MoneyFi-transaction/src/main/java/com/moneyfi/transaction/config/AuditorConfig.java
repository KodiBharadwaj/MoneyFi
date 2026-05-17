package com.moneyfi.transaction.config;

import com.moneyfi.transaction.batch.config.AuditContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

@Configuration
public class AuditorConfig {
    @Bean
    public AuditorAware<Long> auditorProvider() {
        return () -> Optional.ofNullable(AuditContext.getCurrentUser());
    }
}
