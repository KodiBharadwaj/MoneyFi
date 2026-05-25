package com.moneyfi.transaction.batch.dto;

import com.moneyfi.transaction.model.expense.ExpenseGoalRelation;
import com.moneyfi.transaction.model.expense.ExpenseModel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class GoalProcessingResult implements Serializable {
    private GoalModelDto goal;
    private ExpenseModel expense;
    private ExpenseGoalRelation relation;
}
