package com.moneyfi.transaction.batch.reader;

import com.moneyfi.transaction.model.expense.ExpenseModel;
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
public class ExpenseReaderClass {

    @Bean
    public JdbcPagingItemReader<ExpenseModel> expenseReader(DataSource dataSource) throws Exception {
        JdbcPagingItemReader<ExpenseModel> reader = new JdbcPagingItemReader<>();
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

        queryProvider.setSelectClause("SELECT et.id, et.amount, et.category_id, et.description, et.user_id");
        queryProvider.setFromClause("""
                FROM expense_table et
                INNER JOIN category_list_table clt ON clt.id = et.category_id
                """);

        queryProvider.setWhereClause("""
                    WHERE et.recurring = 1
                      AND et.is_deleted = 0
                      AND clt.category NOT IN ('Goal')
                      AND CAST(et.date AS DATE) >= :startLastMonth
                      AND CAST(et.date AS DATE) < :startThisMonth
                      AND NOT EXISTS (
                            SELECT 1
                            FROM expense_table existing
                            WHERE existing.recurring = 1
                              AND existing.user_id = et.user_id
                              AND existing.category_id = et.category_id
                              AND existing.description = et.description
                              AND existing.amount = et.amount
                              AND existing.is_deleted = 0
                              AND CAST(existing.date AS DATE) >= :startThisMonth
                        )
                """);

        queryProvider.setSortKey("id");

        reader.setQueryProvider(queryProvider.getObject());
        reader.setParameterValues(params);
        reader.setRowMapper(new BeanPropertyRowMapper<>(ExpenseModel.class));

        return reader;
    }
}
