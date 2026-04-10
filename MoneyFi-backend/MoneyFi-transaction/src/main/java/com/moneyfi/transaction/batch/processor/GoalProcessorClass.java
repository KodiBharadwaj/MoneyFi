package com.moneyfi.transaction.batch.processor;

import com.moneyfi.constants.enums.TransactionServiceType;
import com.moneyfi.transaction.batch.dto.GoalModelDto;
import com.moneyfi.transaction.batch.dto.GoalProcessingResult;
import com.moneyfi.transaction.model.expense.ExpenseGoalRelation;
import com.moneyfi.transaction.model.expense.ExpenseModel;
import com.moneyfi.transaction.service.transaction.TransactionService;
import com.moneyfi.transaction.utils.enums.EntryModeEnum;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
public class GoalProcessorClass {

    @Autowired(required = false)
    private TransactionService transactionService;

    @Bean
    public ItemProcessor<GoalModelDto, GoalProcessingResult> goalProcessor() {
        LocalDateTime currentTime = LocalDateTime.now();
        Integer categoryId = transactionService.getCategoryWiseList(TransactionServiceType.EXPENSE.name());

        return goal -> {
            BigDecimal remaining = goal.getTargetAmount().subtract(goal.getCurrentAmount());
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                return null;
            }

            BigDecimal contribution = goal.getRecurringAmount().min(remaining);

            ExpenseModel expense = ExpenseModel.builder()
                    .userId(goal.getUserId())
                    .categoryId(categoryId)
                    .amount(contribution)
                    .date(currentTime)
                    .recurring(Boolean.TRUE)
                    .entryMode(EntryModeEnum.RECURRING.name())
                    .description("Auto contribution for goal: " + goal.getGoalName())
                    .build();

            goal.setCurrentAmount(goal.getCurrentAmount().add(contribution));
            goal.setUpdatedAt(currentTime);

            ExpenseGoalRelation relation = ExpenseGoalRelation.builder()
                    .goalId(goal.getId())
                    .build();

            return new GoalProcessingResult(goal, expense, relation);
        };
    }
}
