package com.moneyfi.transaction.batch.listener.goal;

import com.moneyfi.transaction.batch.dto.GoalProcessingResult;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.item.Chunk;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.moneyfi.transaction.utils.constants.StringConstants.*;
import static com.moneyfi.constants.constants.CommonConstants.USER_ID;

@Component
public class GoalWriteListener implements ItemWriteListener<GoalProcessingResult> {

    @Override
    public void afterWrite(Chunk<? extends GoalProcessingResult> items) {
        StepExecution stepExecution = StepSynchronizationManager.getContext().getStepExecution();

        Long userId = stepExecution.getJobParameters().getLong(USER_ID);
        if (userId == null) {
            return;
        }

        List<GoalProcessingResult> goalProcessingResults = (List<GoalProcessingResult>) stepExecution.getExecutionContext().get(PROCESSED_GOALS);

        if (goalProcessingResults == null) {
            goalProcessingResults = new ArrayList<>();
        }

        goalProcessingResults.addAll(items.getItems());
        stepExecution.getExecutionContext().put(PROCESSED_GOALS, goalProcessingResults);
    }
}
