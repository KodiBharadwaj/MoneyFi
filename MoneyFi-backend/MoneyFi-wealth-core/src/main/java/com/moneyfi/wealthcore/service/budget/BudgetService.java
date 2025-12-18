package com.moneyfi.wealthcore.service.budget;

import com.moneyfi.wealthcore.model.BudgetModel;
import com.moneyfi.wealthcore.service.budget.dto.request.AddBudgetDto;
import com.moneyfi.wealthcore.service.budget.dto.response.BudgetDetailsDto;
import com.moneyfi.wealthcore.service.budget.dto.response.SpendingAnalysisResponseDto;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface BudgetService {

    void saveBudget(List<AddBudgetDto> budgetList, Long userId);

    List<BudgetDetailsDto> getAllBudgetsByUserIdAndCategory(Long userId, int month, int year, String category);

    BigDecimal budgetProgress(Long userId, int month, int year);

    void updateBudget(Long userId, List<BudgetModel> budgetList);

    void deleteBudget(Long userId);
}
