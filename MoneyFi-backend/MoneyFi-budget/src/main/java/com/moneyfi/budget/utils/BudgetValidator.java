package com.moneyfi.budget.utils;

import com.moneyfi.budget.exceptions.ScenarioNotPossibleException;
import com.moneyfi.budget.service.dto.request.AddBudgetDto;

import java.math.BigDecimal;
import java.util.List;

public class BudgetValidator {

    private BudgetValidator () {}

    private static final String INPUT_FIELDS_EMPTY = "Input fields cannot be null";
    private static final String TOTAL_PERCENTAGE_MISMATCH = "Total percentage should equals to 100";
    private static final String CATEGORY_FIELD_EMPTY = "Category should not be empty";

    public static void validateInputBudgetRequestDto(List<AddBudgetDto> budgetList) {
        Integer totalPercentage = 0;
        for(AddBudgetDto budget : budgetList) {
            totalPercentage += budget.getPercentage();
            if(budget.getCategory() == null || budget.getMoneyLimit() == null) {
                throw new ScenarioNotPossibleException(INPUT_FIELDS_EMPTY);
            }
            if(budget.getCategory().trim().isEmpty()) throw new ScenarioNotPossibleException(CATEGORY_FIELD_EMPTY);
            if(budget.getPercentage() < 0) throw new ScenarioNotPossibleException("Percentage for " + budget.getCategory() + " should not be negative");
            if(budget.getMoneyLimit().compareTo(BigDecimal.ZERO) < 0) throw new ScenarioNotPossibleException("Money Limit for " + budget.getMoneyLimit() + " should not be negative");
        }
        if(totalPercentage != 100) throw new ScenarioNotPossibleException(TOTAL_PERCENTAGE_MISMATCH);
    }
}
