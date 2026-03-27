package com.moneyfi.transaction.batch.reader;

import com.moneyfi.transaction.model.income.IncomeModel;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;

@Configuration
public class TransactionReader {

    @Bean
    public JdbcPagingItemReader<IncomeModel> reader(DataSource dataSource) throws Exception {
        JdbcPagingItemReader<IncomeModel> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(dataSource);
        reader.setPageSize(1000);

        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(dataSource);
        queryProvider.setSelectClause("SELECT id, amount, category_id, source, user_id");
        queryProvider.setFromClause("FROM income_table");
        queryProvider.setWhereClause("WHERE recurring = 1 AND is_deleted = 0");

        reader.setQueryProvider(queryProvider.getObject());
        reader.setRowMapper(new BeanPropertyRowMapper<>(IncomeModel.class));

        return reader;
    }
}
