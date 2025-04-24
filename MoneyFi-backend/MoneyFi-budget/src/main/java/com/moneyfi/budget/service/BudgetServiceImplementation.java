package com.moneyfi.budget.service;

import com.moneyfi.budget.model.BudgetModel;
import com.moneyfi.budget.repository.BudgetRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;


@Service
public class BudgetServiceImplementation implements BudgetService{

    private final BudgetRepository budgetRepository;
    private final RestTemplate restTemplate;

    public BudgetServiceImplementation(BudgetRepository budgetRepository,
                                       RestTemplate restTemplate){
        this.budgetRepository = budgetRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    public BudgetModel save(BudgetModel budget) {
        return budgetRepository.save(budget);
    }

    @Override
    public List<BudgetModel> getAllBudgetsByUserIdAndCategory(Long userId, String category) {

        List<BudgetModel> budgetList = budgetRepository.getBudgetsByUserId(userId);
        if(category.equalsIgnoreCase("all")){
            return budgetList.stream()
                    .sorted((a,b)-> Long.compare(a.getId(), b.getId()))
                    .toList();
        }

        return budgetList.stream()
                .filter(i -> i.getCategory().equalsIgnoreCase(category))
                .sorted((a,b) -> Long.compare(a.getId(), b.getId()))
                .toList();
    }

    @Override
    public BigDecimal budgetProgress(Long userId, int month, int year) {

        List<BudgetModel> budgetsList = getAllBudgetsByUserIdAndCategory(userId, "all");
        BigDecimal moneyLimit = budgetsList
                            .stream()
                            .map(i->i.getMoneyLimit())
                            .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));

        BigDecimal currentSpending = getTotalExpenseInMonthAndYear(userId, month, year);

        return currentSpending.divide(moneyLimit, 5, RoundingMode.HALF_UP);
    }

    private BigDecimal getTotalExpenseInMonthAndYear(Long userId, int month, int year) {
        BigDecimal totalExpense = budgetRepository.getTotalExpenseInMonthAndYear(userId, month, year);
        if(totalExpense == null){
            return BigDecimal.ZERO;
        }

        return totalExpense;
    }

    @Override
    public BudgetModel update(Long id, BudgetModel budget) {
        System.out.println(budget);
        BudgetModel budgetModel = budgetRepository.findById(id).orElse(null);

        if(budget.getCategory() != null){
            budgetModel.setCategory(budget.getCategory());
        }
        if(budget.getCurrentSpending().compareTo(BigDecimal.ZERO) > 0){
            budgetModel.setCurrentSpending(budget.getCurrentSpending());
        }
        if(budget.getMoneyLimit().compareTo(BigDecimal.ZERO) > 0){
            budgetModel.setMoneyLimit(budget.getMoneyLimit());
        }

        return save(budgetModel);
    }
}
