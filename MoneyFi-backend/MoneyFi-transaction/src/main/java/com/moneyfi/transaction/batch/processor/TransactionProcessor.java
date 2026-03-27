package com.moneyfi.transaction.batch.processor;

import com.moneyfi.transaction.model.income.IncomeModel;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class TransactionProcessor {

    @Bean
    public ItemProcessor<IncomeModel, IncomeModel> processor() {
        return income -> {
            IncomeModel newIncome = new IncomeModel();
            newIncome.setAmount(income.getAmount());
            newIncome.setCategoryId(income.getCategoryId());
            newIncome.setSource(income.getSource());
            newIncome.setUserId(income.getUserId());
            newIncome.setDate(LocalDateTime.now());
            newIncome.setRecurring(true);
//            newIncome.setIsDeleted(false);
            return newIncome;
        };
    }
}
