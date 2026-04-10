package com.moneyfi.transaction.batch.processor;

import com.moneyfi.transaction.model.income.IncomeModel;
import com.moneyfi.transaction.utils.enums.EntryModeEnum;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class IncomeProcessor {

    @Bean
    public ItemProcessor<IncomeModel, IncomeModel> processor() {
        LocalDateTime currentTime = LocalDateTime.now();
        return income -> {
            return IncomeModel.builder()
                    .userId(income.getUserId())
                    .amount(income.getAmount())
                    .categoryId(income.getCategoryId())
                    .source(income.getSource())
                    .date(currentTime)
                    .recurring(Boolean.TRUE)
                    .isDeleted(Boolean.FALSE)
                    .description(income.getDescription())
                    .entryMode(EntryModeEnum.MANUAL.name())
                    .createdAt(currentTime)
                    .updatedAt(currentTime)
                    .build();
        };
    }
}
