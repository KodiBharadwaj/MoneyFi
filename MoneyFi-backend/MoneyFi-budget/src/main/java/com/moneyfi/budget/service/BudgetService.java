package com.moneyfi.budget.service;

import com.moneyfi.budget.model.BudgetModel;

import java.math.BigDecimal;
import java.util.List;

public interface BudgetService {

    BudgetModel save(BudgetModel budget);

    List<BudgetModel> getAllBudgets(Long userId);

    BigDecimal budgetProgress(Long userId, int month, int year);

    BudgetModel update(Long id, BudgetModel budget);
}
