package com.moneyfi.transaction.batch.config;

import com.moneyfi.transaction.model.expense.ExpenseModel;
import com.moneyfi.transaction.model.income.IncomeModel;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    @Bean
    public Job incomeJob(JobRepository jobRepository, Step incomeStep) {
        return new JobBuilder("recurring-income-job", jobRepository).start(incomeStep).build();
    }

    @Bean
    public Step incomeStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                           JdbcPagingItemReader<IncomeModel> reader, ItemProcessor<IncomeModel, IncomeModel> processor, JdbcBatchItemWriter<IncomeModel> writer) {
        return new StepBuilder("recurring-income-step", jobRepository)
                .<IncomeModel, IncomeModel>chunk(1000, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job expenseJob(JobRepository jobRepository, Step expenseStep) {
        return new JobBuilder("recurring-expense-job", jobRepository)
                .start(expenseStep)
                .build();
    }

    @Bean
    public Step expenseStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, JdbcPagingItemReader<ExpenseModel> expenseReader,
                            ItemProcessor<ExpenseModel, ExpenseModel> expenseProcessor, JdbcBatchItemWriter<ExpenseModel> expenseWriter) {

        return new StepBuilder("recurring-expense-step", jobRepository)
                .<ExpenseModel, ExpenseModel>chunk(1000, transactionManager)
                .reader(expenseReader)
                .processor(expenseProcessor)
                .writer(expenseWriter)
                .build();
    }
}
