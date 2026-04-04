package com.moneyfi.transaction.batch.reader;

import com.moneyfi.transaction.batch.dto.GoalModelDto;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class GoalReaderClass {

    @Bean
    public JdbcPagingItemReader<GoalModelDto> goalReader(DataSource dataSource) {
        JdbcPagingItemReader<GoalModelDto> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(dataSource);
        reader.setPageSize(100);

        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(dataSource);

        queryProvider.setSelectClause("SELECT *");
        queryProvider.setFromClause("""
                    FROM goal_table g
                    WHERE g.deleted = 0
                      AND g.current_amount < g.target_amount
                      AND (
                            DAY(g.created_at) = DAY(GETDATE())
                            OR (
                                DAY(g.created_at) > DAY(EOMONTH(GETDATE()))
                                AND DAY(GETDATE()) = DAY(EOMONTH(GETDATE()))
                            )
                          )
                      AND NOT EXISTS (
                            SELECT 1
                            FROM expense_goal_relation_table egr
                            INNER JOIN expense_table e ON e.id = egr.expense_id
                            WHERE egr.goal_id = g.id
                              AND e.is_deleted = 0
                              AND YEAR(e.date) = YEAR(GETDATE())
                              AND MONTH(e.date) = MONTH(GETDATE())
                              AND e.entry_mode = 'GOAL_AUTO'
                      )
                """);

        queryProvider.setSortKey("g.id");

        try {
            reader.setQueryProvider(queryProvider.getObject());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        reader.setRowMapper((rs, rowNum) -> GoalModelDto.builder()
                .id(rs.getLong("id"))
                .userId(rs.getLong("user_id"))
                .goalName(rs.getString("goal_name"))
                .currentAmount(rs.getBigDecimal("current_amount"))
                .recurringAmount(rs.getBigDecimal("recurring_amount"))
                .targetAmount(rs.getBigDecimal("target_amount"))
                .deadLine(rs.getTimestamp("dead_line_date").toLocalDateTime())
                .categoryId(rs.getInt("category_id"))
                .deleted(rs.getBoolean("deleted"))
                .description(rs.getString("description"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                .build());
        return reader;
    }
}
