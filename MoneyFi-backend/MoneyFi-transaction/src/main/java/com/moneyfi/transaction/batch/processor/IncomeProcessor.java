package com.moneyfi.transaction.batch.processor;

import com.moneyfi.transaction.model.income.IncomeModel;
import com.moneyfi.transaction.utils.enums.EntryModeEnum;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

import static com.moneyfi.transaction.utils.constants.StringConstants.*;

@Configuration
public class IncomeProcessor {

    @Bean
    @StepScope
    public ItemProcessor<IncomeModel, IncomeModel> processor(@Value("#{jobParameters['adminUserId']}") String adminUserId) {
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
                    .entryMode(EntryModeEnum.RECURRING.name() + UNDERSCORE + (!adminUserId.equalsIgnoreCase(BATCH_AUTO_TRIGGER) ? BATCH_MANUAL_TRIGGER + UNDERSCORE + ADMIN + UNDERSCORE + adminUserId.split(ADMIN + UNDERSCORE)[1] : BATCH_AUTO_TRIGGER))
                    .createdAt(currentTime)
                    .updatedAt(currentTime)
                    .build();
        };
    }
}
