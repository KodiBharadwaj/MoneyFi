package com.moneyfi.transaction.batch.writer;

import com.moneyfi.transaction.model.income.IncomeModel;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class IncomeWriter {

    @Bean
    public JdbcBatchItemWriter<IncomeModel> writer(DataSource dataSource) {
        JdbcBatchItemWriter<IncomeModel> writer = new JdbcBatchItemWriter<>();
        writer.setDataSource(dataSource);

        writer.setSql("""
            INSERT INTO income_table 
            (user_id, amount, category_id, source, date, recurring, is_deleted, description, entry_mode, created_at, updated_at)
            VALUES 
            (:userId, :amount, :categoryId, :source, :date, :recurring, :isDeleted, :description, :entryMode, :createdAt, :updatedAt)
        """);

        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        return writer;
    }
}
