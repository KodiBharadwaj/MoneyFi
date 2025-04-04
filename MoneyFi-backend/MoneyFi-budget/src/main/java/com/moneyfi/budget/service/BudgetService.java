package com.moneyfi.budget.service;

import com.moneyfi.budget.model.BudgetModel;

import java.util.List;

public interface BudgetService {

    public BudgetModel save(BudgetModel budget);

    public List<BudgetModel> getAllBudgets(Long userId);

    public Double budgetProgress(Long userId, int month, int year);

    public BudgetModel update(Long id, BudgetModel budget);
}
