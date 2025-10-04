package com.moneyfi.budget.repository.common;

import com.moneyfi.budget.service.dto.response.BudgetDetailsDto;
import com.moneyfi.budget.service.dto.response.UserDetailsForSpendingAnalysisDto;

import java.util.List;

public interface BudgetCommonRepository {
    List<BudgetDetailsDto> getBudgetsByUserId(Long userId, int month, int year, String category);

    UserDetailsForSpendingAnalysisDto getUserDetailsForAccountSpendingAnalysisStatement(Long userId);
}
