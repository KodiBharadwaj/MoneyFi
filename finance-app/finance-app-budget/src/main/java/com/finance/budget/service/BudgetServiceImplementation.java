package com.finance.budget.service;

import com.finance.budget.model.BudgetModel;
import com.finance.budget.repository.BudgetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BudgetServiceImplementation implements BudgetService{

    @Autowired
    private BudgetRepository budgetRepository;

    @Override
    public BudgetModel save(BudgetModel budget) {
        return budgetRepository.save(budget);
    }

    @Override
    public List<BudgetModel> getAllBudgets(int userId) {

        return budgetRepository.getBudgetsByUserId(userId).stream().sorted((a,b)->a.getId()-b.getId()).toList();
    }

    @Override
    public BudgetModel update(int id, BudgetModel budget) {
        System.out.println(budget);
        BudgetModel budgetModel = budgetRepository.findById(id).orElse(null);

        if(budget.getCategory() != null){
            budgetModel.setCategory(budget.getCategory());
        }
        if(budget.getCurrentSpending() > 0){
            budgetModel.setCurrentSpending(budget.getCurrentSpending());
        }
        if(budget.getMoneyLimit() > 0){
            budgetModel.setMoneyLimit(budget.getMoneyLimit());
        }

        return save(budgetModel);
    }
}
