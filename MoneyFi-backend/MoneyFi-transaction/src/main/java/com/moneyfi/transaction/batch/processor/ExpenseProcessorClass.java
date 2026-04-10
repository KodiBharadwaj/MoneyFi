package com.moneyfi.transaction.batch.processor;

import com.moneyfi.transaction.model.expense.ExpenseModel;
import com.moneyfi.transaction.utils.enums.EntryModeEnum;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class ExpenseProcessorClass {

    @Bean
    public ItemProcessor<ExpenseModel, ExpenseModel> expenseProcessor() {
        LocalDateTime currentTime = LocalDateTime.now();

        return expense -> ExpenseModel.builder()
                .userId(expense.getUserId())
                .amount(expense.getAmount())
                .categoryId(expense.getCategoryId())
                .date(currentTime)
                .recurring(Boolean.TRUE)
                .isDeleted(Boolean.FALSE)
                .description(expense.getDescription())
                .entryMode(EntryModeEnum.MANUAL.name())
                .createdAt(currentTime)
                .updatedAt(currentTime)
                .build();
    }
}
