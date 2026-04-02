package com.moneyfi.transaction.batch.reader;

import com.moneyfi.transaction.model.income.IncomeModel;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class IncomeReader {

    @Bean
    public JdbcPagingItemReader<IncomeModel> reader(DataSource dataSource) throws Exception {
        JdbcPagingItemReader<IncomeModel> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(dataSource);
        reader.setPageSize(1000);

        LocalDate now = LocalDate.now();
        LocalDate firstDayOfThisMonth = now.withDayOfMonth(1);
        LocalDate firstDayOfLastMonth = firstDayOfThisMonth.minusMonths(1);

        Map<String, Object> params = new HashMap<>();
        params.put("startLastMonth", firstDayOfLastMonth);
        params.put("startThisMonth", firstDayOfThisMonth);

        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(dataSource);

        queryProvider.setSelectClause("SELECT id, amount, category_id, source, user_id");
        queryProvider.setFromClause("FROM income_table it");
        queryProvider.setWhereClause("""
            WHERE it.recurring = 1
              AND it.is_deleted = 0
              AND CAST(it.date AS DATE) >= :startLastMonth
              AND CAST(it.date AS DATE) < :startThisMonth
              AND NOT EXISTS (
                    SELECT 1
                    FROM income_table existing
                    WHERE existing.recurring = 1
                      AND existing.user_id = it.user_id
                      AND existing.category_id = it.category_id
                      AND existing.source = it.source
                      AND existing.amount = it.amount
                      AND CAST(existing.date AS DATE) >= :startThisMonth
                )
        """);
        queryProvider.setSortKey("id");

        reader.setQueryProvider(queryProvider.getObject());
        reader.setParameterValues(params);
        reader.setRowMapper(new BeanPropertyRowMapper<>(IncomeModel.class));

        return reader;
    }
}
