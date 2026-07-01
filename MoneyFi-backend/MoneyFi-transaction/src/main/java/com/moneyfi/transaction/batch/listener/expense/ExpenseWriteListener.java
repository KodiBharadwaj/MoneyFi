package com.moneyfi.transaction.batch.listener.expense;

import com.moneyfi.transaction.model.expense.ExpenseModel;
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
public class ExpenseWriteListener implements ItemWriteListener<ExpenseModel> {

    @Override
    public void afterWrite(Chunk<? extends ExpenseModel> items) {
        StepExecution stepExecution = StepSynchronizationManager.getContext().getStepExecution();

        Long userId = stepExecution.getJobParameters().getLong(USER_ID);
        if (userId == null) {
            return;
        }

        List<ExpenseModel> expenses = (List<ExpenseModel>) stepExecution.getExecutionContext().get(PROCESSED_EXPENSES);

        if (expenses == null) {
            expenses = new ArrayList<>();
        }

        expenses.addAll(items.getItems());
        stepExecution.getExecutionContext().put(PROCESSED_EXPENSES, expenses);
    }
}
