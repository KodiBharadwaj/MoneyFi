package com.moneyfi.transaction.batch.listener.income;

import com.moneyfi.transaction.model.income.IncomeModel;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.item.Chunk;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.moneyfi.transaction.utils.constants.StringConstants.PROCESSED_INCOMES;
import static com.moneyfi.transaction.utils.constants.StringConstants.USER_ID;

@Component
public class IncomeWriteListener implements ItemWriteListener<IncomeModel> {

    @Override
    public void afterWrite(Chunk<? extends IncomeModel> items) {
        StepExecution stepExecution = StepSynchronizationManager.getContext().getStepExecution();

        Long userId = stepExecution.getJobParameters().getLong(USER_ID);
        if (userId == null) {
            return;
        }

        List<IncomeModel> incomes = (List<IncomeModel>) stepExecution.getExecutionContext().get(PROCESSED_INCOMES);

        if (incomes == null) {
            incomes = new ArrayList<>();
        }

        incomes.addAll(items.getItems());
        stepExecution.getExecutionContext().put(PROCESSED_INCOMES, incomes);
    }
}
