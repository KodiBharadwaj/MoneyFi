package com.moneyfi.transaction.batch.listener.goal;

import com.moneyfi.constants.dto.BatchInfoForEmailDto;
import com.moneyfi.constants.enums.TransactionServiceType;
import com.moneyfi.transaction.batch.dto.GoalModelDto;
import com.moneyfi.transaction.batch.dto.GoalProcessingResult;
import com.moneyfi.transaction.batch.service.general.BatchAuthTokenStore;
import com.moneyfi.transaction.model.income.IncomeModel;
import com.moneyfi.transaction.service.external.api.ExternalApiCallService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.moneyfi.transaction.utils.constants.StringConstants.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class GoalJobListener implements JobExecutionListener {

    private final ExternalApiCallService externalApiCallService;
    private final BatchAuthTokenStore batchAuthTokenStore;

    @Override
    public void afterJob(JobExecution jobExecution) {

        if (jobExecution.getStatus() != BatchStatus.COMPLETED) {
            return;
        }

        Long userId = jobExecution.getJobParameters().getLong(USER_ID);

        if (userId == null) {
            return;
        }

        for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
            List<GoalProcessingResult> goals = (List<GoalProcessingResult>) stepExecution.getExecutionContext().get(PROCESSED_GOALS);

            if (goals == null) {
                continue;
            }

            List<BatchInfoForEmailDto> batchInfoList = new ArrayList<>();
            for (GoalProcessingResult goalProcessingDto : goals) {
                GoalModelDto goal = goalProcessingDto.getGoal();
                BatchInfoForEmailDto dto = new BatchInfoForEmailDto();
                dto.setUserId(userId);
                dto.setTransactionType(TransactionServiceType.GOAL.name());
                dto.setDescription(goal.getGoalName());
                dto.setAmount(goal.getRecurringAmount());
                batchInfoList.add(dto);
            }

            String requestId = jobExecution.getJobParameters().getString(REQUEST_ID);
            String authToken = batchAuthTokenStore.get(requestId);

            log.info("Verifying admin token: {}", authToken);
            if (authToken != null) {
                batchAuthTokenStore.remove(requestId);
                externalApiCallService.externalCallToUserServiceToSendEmailToUser(batchInfoList, authToken);
            }
        }
    }
}
