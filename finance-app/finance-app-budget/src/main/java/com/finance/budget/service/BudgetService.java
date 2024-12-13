package com.finance.budget.service;

import com.finance.budget.model.BudgetModel;

import java.util.List;

public interface BudgetService {

    public BudgetModel save(BudgetModel budget);

    public BudgetModel addToBudget(BudgetModel budget);

    public List<BudgetModel> getAllBudgets(int userId);

    public BudgetModel update(int id, BudgetModel budget);

    public void deleteParticularBudgetByCategory(int userId, String category);
}
