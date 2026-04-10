package com.moneyfi.transaction.batch.config;

import com.moneyfi.transaction.batch.dto.GoalModelDto;
import com.moneyfi.transaction.batch.dto.GoalProcessingResult;
import com.moneyfi.transaction.model.expense.ExpenseModel;
import com.moneyfi.transaction.model.income.IncomeModel;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
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
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(100)
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

    @Bean
    public Job goalJob(JobRepository jobRepository, Step goalStep) {
        return new JobBuilder("recurring-goal-job", jobRepository)
                .start(goalStep)
                .build();
    }

    @Bean
    public Step goalStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, JdbcPagingItemReader<GoalModelDto> goalReader,
                         ItemProcessor<GoalModelDto, GoalProcessingResult> goalProcessor, ItemWriter<GoalProcessingResult> goalWriter) {

        return new StepBuilder("recurring-goal-step", jobRepository)
                .<GoalModelDto, GoalProcessingResult>chunk(1000, transactionManager)
                .reader(goalReader)
                .processor(goalProcessor)
                .writer(goalWriter)
                .build();
    }
}
