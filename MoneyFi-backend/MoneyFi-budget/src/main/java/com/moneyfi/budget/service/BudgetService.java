package com.moneyfi.budget.service;

import com.moneyfi.budget.model.BudgetModel;
import com.moneyfi.budget.service.dto.request.AddBudgetDto;
import com.moneyfi.budget.service.dto.response.BudgetDetailsDto;
import com.moneyfi.budget.service.dto.response.SpendingAnalysisResponseDto;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface BudgetService {

    void saveBudget(List<AddBudgetDto> budgetList, Long userId);

    List<BudgetDetailsDto> getAllBudgetsByUserIdAndCategory(Long userId, int month, int year, String category);

    BigDecimal budgetProgress(Long userId, int month, int year);

    void updateBudget(Long userId, List<BudgetModel> budgetList);

    SpendingAnalysisResponseDto getUserSpendingAnalysisByBudgetCategories(Long userId, LocalDate fromDate, LocalDate toDate, String authHeader);

    byte[] getUserSpendingAnalysisByBudgetCategoriesPdf(Long userId, LocalDate fromDate, LocalDate toDate, String authHeader);

    ResponseEntity<String> getUserSpendingAnalysisByBudgetCategoriesPdfEmail(Long userId, LocalDate fromDate, LocalDate toDate, String authHeader);

    void deleteBudget(Long userId);
}
