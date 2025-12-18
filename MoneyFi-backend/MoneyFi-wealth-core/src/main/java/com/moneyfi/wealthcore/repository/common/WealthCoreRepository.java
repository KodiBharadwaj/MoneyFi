package com.moneyfi.wealthcore.repository.common;

import com.moneyfi.wealthcore.service.budget.dto.response.BudgetDetailsDto;
import com.moneyfi.wealthcore.service.budget.dto.response.UserDetailsForSpendingAnalysisDto;
import com.moneyfi.wealthcore.service.goal.dto.response.GoalDetailsDto;

import java.util.List;

public interface WealthCoreRepository {

    List<BudgetDetailsDto> getBudgetsByUserId(Long userId, int month, int year, String category);

    UserDetailsForSpendingAnalysisDto getUserDetailsForAccountSpendingAnalysisStatement(Long userId);

    List<GoalDetailsDto> getAllGoalsByUserId(Long userId);
}
