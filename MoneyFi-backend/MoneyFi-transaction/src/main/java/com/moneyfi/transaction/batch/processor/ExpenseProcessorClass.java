package com.moneyfi.transaction.batch.processor;

import com.moneyfi.transaction.model.expense.ExpenseModel;
import com.moneyfi.transaction.utils.enums.EntryModeEnum;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
public class ExpenseProcessorClass {

    @Bean
    @StepScope
    public ItemProcessor<ExpenseModel, ExpenseModel> expenseProcessor(@Value("#{jobParameters['inputDate']}") LocalDate inputDate) {

        LocalDateTime transactionTime = inputDate.atStartOfDay();
        LocalDateTime currentTime = LocalDateTime.now();
        return expense -> ExpenseModel.builder()
                .userId(expense.getUserId())
                .amount(expense.getAmount())
                .categoryId(expense.getCategoryId())
                .date(transactionTime)
                .recurring(Boolean.TRUE)
                .isDeleted(Boolean.FALSE)
                .description(expense.getDescription())
                .entryMode(EntryModeEnum.MANUAL.name())
                .createdAt(currentTime)
                .updatedAt(currentTime)
                .build();
    }
}
