package com.moneyfi.transaction.batch.writer;

import com.moneyfi.transaction.batch.dto.UserNotification;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class UserSchedulingNotificationWriter {

    @Bean
    public JdbcBatchItemWriter<UserNotification> writer(DataSource dataSource) {
        JdbcBatchItemWriter<UserNotification> writer = new JdbcBatchItemWriter<>();
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.setSql("INSERT INTO user_notifications (user_id, message, created_at) VALUES (:userId, :message, :createdAt)");
        writer.setDataSource(dataSource);
        return writer;
    }
}

