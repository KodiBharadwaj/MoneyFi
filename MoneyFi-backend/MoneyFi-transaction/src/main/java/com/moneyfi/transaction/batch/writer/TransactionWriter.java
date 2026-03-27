package com.moneyfi.transaction.batch.writer;

import com.moneyfi.transaction.model.income.IncomeModel;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class TransactionWriter {

    @Bean
    public JdbcBatchItemWriter<IncomeModel> writer(DataSource dataSource) {
        JdbcBatchItemWriter<IncomeModel> writer = new JdbcBatchItemWriter<>();
        writer.setDataSource(dataSource);

        writer.setSql("""
        INSERT INTO income_table 
        (amount, category_id, date, is_deleted, recurring, source, user_id)
        VALUES (:amount, :categoryId, :date, :isDeleted, :recurring, :source, :userId)
    """);

        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        return writer;
    }
}
