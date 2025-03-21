package com.finance.budget.service;

import com.finance.budget.model.BudgetModel;

import java.util.List;

public interface BudgetService {

    public BudgetModel save(BudgetModel budget);

    public List<BudgetModel> getAllBudgets(int userId);

    public Double budgetProgress(int userId, int month, int year);

    public BudgetModel update(int id, BudgetModel budget);
}
