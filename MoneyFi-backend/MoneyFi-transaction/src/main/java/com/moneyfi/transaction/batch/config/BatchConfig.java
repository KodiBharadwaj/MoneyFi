package com.moneyfi.transaction.batch.config;

import com.moneyfi.transaction.model.income.IncomeModel;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.boot.autoconfigure.batch.BatchProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import static org.apache.commons.io.IOUtils.writer;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    @Bean
    public Step step(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("recurring-step", jobRepository)
                .<IncomeModel, IncomeModel>chunk(1000, transactionManager)
                .reader(reader(null))
                .processor(processor())
                .writer(writer(null))
                .build();
    }

    @Bean
    public BatchProperties.Job job(JobRepository jobRepository, Step step) {
        return new JobBuilder("recurring-job", jobRepository)
                .start(step)
                .build();
    }
}
