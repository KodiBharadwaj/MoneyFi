package com.moneyfi.budget.service;

import com.moneyfi.budget.model.BudgetModel;
import com.moneyfi.budget.service.dto.request.AddBudgetDto;
import com.moneyfi.budget.service.dto.response.BudgetDetailsDto;

import java.math.BigDecimal;
import java.util.List;

public interface BudgetService {

    void save(List<AddBudgetDto> budgetList, Long userId);

    List<BudgetDetailsDto> getAllBudgetsByUserIdAndCategory(Long userId, int month, int year, String category);

    BigDecimal budgetProgress(Long userId, int month, int year);

    BudgetModel update(Long id, Long userId, BudgetModel budget);
}
