package com.moneyfi.transaction.batch.writer;

import com.moneyfi.transaction.batch.dto.GoalProcessingResult;
import com.moneyfi.transaction.model.expense.ExpenseGoalRelation;
import com.moneyfi.transaction.model.expense.ExpenseModel;
import com.moneyfi.transaction.repository.expense.ExpenseGoalRelationRepository;
import com.moneyfi.transaction.repository.expense.ExpenseRepository;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class GoalWriterClass {

    @Bean
    public ItemWriter<GoalProcessingResult> goalWriter(ExpenseRepository expenseRepository, ExpenseGoalRelationRepository relationRepository, JdbcTemplate jdbcTemplate) {
        return items -> {

            List<ExpenseModel> expenses = new ArrayList<>();
            for (GoalProcessingResult item : items) {
                expenses.add(item.getExpense());
            }
            List<ExpenseModel> savedExpenses = expenseRepository.saveAll(expenses);

            List<ExpenseGoalRelation> relations = new ArrayList<>();
            for (int i = 0; i < items.size(); i++) {
                GoalProcessingResult item = items.getItems().get(i);
                ExpenseGoalRelation relation = item.getRelation();
                relation.setExpense(savedExpenses.get(i));
                relations.add(relation);
            }
            relationRepository.saveAll(relations);

            String sql = """
                UPDATE goal_table
                SET current_amount =
                        CASE 
                            WHEN current_amount + ? > target_amount 
                            THEN target_amount
                            ELSE current_amount + ?
                        END,
                    updated_at = ?
                WHERE id = ?
                  AND deleted = 0
                """;

            List<Object[]> batchArgs = new ArrayList<>();

            for (GoalProcessingResult item : items) {
                BigDecimal amount = item.getExpense().getAmount();
                batchArgs.add(new Object[]{
                        amount,
                        amount,
                        Timestamp.valueOf(LocalDateTime.now()),
                        item.getGoal().getId()
                });
            }

            jdbcTemplate.batchUpdate(sql, batchArgs);
        };
    }
}
