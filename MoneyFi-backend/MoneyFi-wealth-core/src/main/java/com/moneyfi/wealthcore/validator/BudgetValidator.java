package com.moneyfi.wealthcore.validator;

import com.moneyfi.wealthcore.exceptions.ScenarioNotPossibleException;
import com.moneyfi.wealthcore.model.BudgetModel;
import com.moneyfi.wealthcore.service.budget.dto.request.AddBudgetDto;

import java.math.BigDecimal;
import java.util.List;

public class BudgetValidator {

    private BudgetValidator () {}

    private static final String INPUT_FIELDS_EMPTY = "Input fields cannot be null";
    private static final String TOTAL_PERCENTAGE_MISMATCH = "Total percentage should equals to 100";
    private static final String CATEGORY_FIELD_EMPTY = "Category should not be empty";

    public static void validateBudgetSaveRequestDto(List<AddBudgetDto> budgetList, BigDecimal totalIncomeInThisMonth) {
        Integer totalPercentage = 0;
        BigDecimal totalBudgetSet = BigDecimal.ZERO;
        for(AddBudgetDto budget : budgetList) {
            totalPercentage += budget.getPercentage();
            totalBudgetSet = totalBudgetSet.add(budget.getMoneyLimit());
            if(budget.getCategory() == null || budget.getMoneyLimit() == null) {
                throw new ScenarioNotPossibleException(INPUT_FIELDS_EMPTY);
            }
            if(budget.getCategory().trim().isEmpty()) throw new ScenarioNotPossibleException(CATEGORY_FIELD_EMPTY);
            if(budget.getPercentage() < 0) throw new ScenarioNotPossibleException("Percentage for " + budget.getCategory() + " should not be negative");
            if(budget.getMoneyLimit().compareTo(BigDecimal.ZERO) < 0) throw new ScenarioNotPossibleException("Budget Limit for " + budget.getMoneyLimit() + " should not be negative");
        }
        if(totalPercentage != 100) throw new ScenarioNotPossibleException(TOTAL_PERCENTAGE_MISMATCH);
        if(totalBudgetSet.compareTo(totalIncomeInThisMonth) > 0) throw new ScenarioNotPossibleException("Total budget entered exceeded the available income by " + totalBudgetSet.compareTo(totalIncomeInThisMonth));
    }

    public static void validateBudgetUpdateRequestDto(List<BudgetModel> budgetList) {
        for(BudgetModel budget : budgetList) {
            if(budget.getCategory() == null || budget.getMoneyLimit() == null) {
                throw new ScenarioNotPossibleException(INPUT_FIELDS_EMPTY);
            }
            if(budget.getCategory().trim().isEmpty()) throw new ScenarioNotPossibleException(CATEGORY_FIELD_EMPTY);
            if(budget.getMoneyLimit().compareTo(BigDecimal.ZERO) < 0) throw new ScenarioNotPossibleException("Budget Limit for " + budget.getCategory() + " should not be negative");
        }
    }
}
