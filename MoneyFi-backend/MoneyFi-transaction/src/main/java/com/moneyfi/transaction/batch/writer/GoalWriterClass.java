package com.moneyfi.transaction.batch.writer;

import com.moneyfi.transaction.batch.dto.GoalProcessingResult;
import com.moneyfi.transaction.model.expense.ExpenseGoalRelation;
import com.moneyfi.transaction.model.expense.ExpenseModel;
import com.moneyfi.transaction.repository.expense.ExpenseGoalRelationRepository;
import com.moneyfi.transaction.repository.expense.ExpenseRepository;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoalWriterClass {

    @Bean
    public ItemWriter<GoalProcessingResult> goalWriter(
            ExpenseRepository expenseRepository,
            GoalRepository goalRepository,
            ExpenseGoalRelationRepository relationRepository
    ) {
        return items -> {

            for (GoalProcessingResult item : items) {

                // 1. Save expense
                ExpenseModel savedExpense = expenseRepository.save(item.getExpense());

                // 2. Save relation
                ExpenseGoalRelation relation = item.getRelation();
                relation.setExpense(savedExpense);
                relationRepository.save(relation);

                // 3. Update goal
                goalRepository.save(item.getGoal());
            }
        };
    }
}
