package com.moneyfi.budget.service;

import com.moneyfi.budget.exceptions.ResourceNotFoundException;
import com.moneyfi.budget.model.BudgetModel;
import com.moneyfi.budget.repository.BudgetRepository;
import com.moneyfi.budget.repository.common.BudgetCommonRepository;
import com.moneyfi.budget.service.dto.request.AddBudgetDto;
import com.moneyfi.budget.service.dto.response.BudgetDetailsDto;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;


@Service
public class BudgetServiceImplementation implements BudgetService{

    private final BudgetRepository budgetRepository;
    private final BudgetCommonRepository budgetCommonRepository;

    public BudgetServiceImplementation(BudgetRepository budgetRepository,
                                       BudgetCommonRepository budgetCommonRepository){
        this.budgetRepository = budgetRepository;
        this.budgetCommonRepository = budgetCommonRepository;
    }

    @Override
    @Transactional
    public void saveBudget(List<AddBudgetDto> budgetList, Long userId) {

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
    public BigDecimal budgetProgress(Long userId, int month, int year) {

        List<BudgetDetailsDto> budgetsList = getAllBudgetsByUserIdAndCategory(userId, month, year, "all");
        BigDecimal moneyLimit = budgetsList
                            .stream()
                            .map(i->i.getMoneyLimit())
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

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
    @Transactional
    public void updateBudget(Long userId, List<BudgetModel> budgetList) {

        for(BudgetModel budget : budgetList){
            BudgetModel budgetModel = budgetRepository.findById(budget.getId()).orElse(null);

            if(budgetModel == null || !budgetModel.getUserId().equals(userId)){
                throw new ResourceNotFoundException("UnAuthorized try");
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

            budgetRepository.save(budgetModel);
        }
    }
}
