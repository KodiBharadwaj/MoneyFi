package com.moneyfi.transaction.batch.processor;

import com.moneyfi.transaction.batch.dto.UserNotification;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserSchedulingNotificationProcessor {

    @Bean
    @StepScope
    public ItemProcessor<String, UserNotification> scheduleNotificationProcessor(@Value("#{jobParameters['scheduleId']}") Long scheduleId) {
        return username -> {
            return UserNotification.builder().username(username).scheduleId(scheduleId).isRead(Boolean.FALSE).build();
        };
    }
}
