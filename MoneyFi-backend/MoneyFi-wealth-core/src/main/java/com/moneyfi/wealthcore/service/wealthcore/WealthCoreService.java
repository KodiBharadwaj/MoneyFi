package com.moneyfi.wealthcore.service.wealthcore;

import com.moneyfi.wealthcore.service.budget.dto.response.SpendingAnalysisResponseDto;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

public interface WealthCoreService {

    SpendingAnalysisResponseDto getUserSpendingAnalysisByBudgetCategories(Long userId, LocalDate fromDate, LocalDate toDate, String authHeader);

    byte[] getUserSpendingAnalysisByBudgetCategoriesPdf(Long userId, LocalDate fromDate, LocalDate toDate, String authHeader);

    ResponseEntity<String> getUserSpendingAnalysisByBudgetCategoriesPdfEmail(Long userId, LocalDate fromDate, LocalDate toDate, String authHeader);

}
