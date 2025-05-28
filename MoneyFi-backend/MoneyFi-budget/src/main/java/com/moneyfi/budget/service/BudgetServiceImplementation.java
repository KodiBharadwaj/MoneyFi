package com.moneyfi.budget.service;

import com.moneyfi.budget.model.BudgetModel;
import com.moneyfi.budget.repository.BudgetRepository;
import com.moneyfi.budget.repository.common.BudgetCommonRepository;
import com.moneyfi.budget.service.dto.request.AddBudgetDto;
import com.moneyfi.budget.service.dto.response.BudgetDetailsDto;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;


@Service
public class BudgetServiceImplementation implements BudgetService{

    private final BudgetRepository budgetRepository;
    private final BudgetCommonRepository budgetCommonRepository;
    private final RestTemplate restTemplate;

    public BudgetServiceImplementation(BudgetRepository budgetRepository,
                                       RestTemplate restTemplate,
                                       BudgetCommonRepository budgetCommonRepository){
        this.budgetRepository = budgetRepository;
        this.restTemplate = restTemplate;
        this.budgetCommonRepository = budgetCommonRepository;
    }

    @Override
    @Transactional
    public void save(List<AddBudgetDto> budgetList, Long userId) {

        for(AddBudgetDto budget : budgetList){
            BudgetModel budgetModel = new BudgetModel();
            budgetModel.setUserId(userId);
            budgetModel.setCategory(budget.getCategory());
            budgetModel.setMoneyLimit(budget.getMoneyLimit());
            budgetRepository.save(budgetModel);
        }
    }

    @Override
    public List<BudgetDetailsDto> getAllBudgetsByUserIdAndCategory(Long userId, int month, int year, String category) {
        return budgetCommonRepository.getBudgetsByUserId(userId, month, year, category);
    }

    @Override
    @Transactional
    public BigDecimal budgetProgress(Long userId, int month, int year) {

        List<BudgetDetailsDto> budgetsList = getAllBudgetsByUserIdAndCategory(userId, month, year, "all");
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
    public BudgetModel update(Long id, Long userId, BudgetModel budget) {
        BudgetModel budgetModel = budgetRepository.findById(id).orElse(null);
        if(budgetModel.getUserId() != userId){
            return null;
        }

        if(budget.getCategory() != null){
            budgetModel.setCategory(budget.getCategory());
        }
        if(budget.getCurrentSpending().compareTo(BigDecimal.ZERO) > 0){
            budgetModel.setCurrentSpending(budget.getCurrentSpending());
        }
        if(budget.getMoneyLimit().compareTo(BigDecimal.ZERO) > 0){
            budgetModel.setMoneyLimit(budget.getMoneyLimit());
        }

        return budgetRepository.save(budgetModel);
    }
}
