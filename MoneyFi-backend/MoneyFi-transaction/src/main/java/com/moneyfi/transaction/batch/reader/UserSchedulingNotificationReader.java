package com.moneyfi.transaction.batch.reader;

import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserSchedulingNotificationReader {

    @Bean
    public JdbcPagingItemReader<String> reader(){
        return null;
    }
}
