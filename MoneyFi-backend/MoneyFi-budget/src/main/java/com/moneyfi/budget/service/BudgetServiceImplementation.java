package com.moneyfi.budget.service;

import com.moneyfi.budget.model.BudgetModel;
import com.moneyfi.budget.repository.BudgetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static java.util.Arrays.stream;

@Service
public class BudgetServiceImplementation implements BudgetService{

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public BudgetModel save(BudgetModel budget) {
        return budgetRepository.save(budget);
    }

    @Override
    public List<BudgetModel> getAllBudgets(int userId) {

        List<BudgetModel> budgetList = budgetRepository.getBudgetsByUserId(userId);
        return budgetList.stream()
                .sorted((a,b)->a.getId()-b.getId()).toList();
    }

    @Override
    public Double budgetProgress(int userId, int month, int year) {

        List<BudgetModel> budgetsList = getAllBudgets(userId);
        double moneyLimit = budgetsList
                            .stream()
                            .mapToDouble(i->i.getMoneyLimit())
                            .sum();

        Double currentSpending = restTemplate.getForObject("http://MONEYFI-EXPENSE/api/expense/" + userId + "/totalExpense/" + month + "/" + year, Double.class);

        return currentSpending/moneyLimit;
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
