package com.moneyfi.budget.repository.common;

import com.moneyfi.budget.service.dto.response.BudgetDetailsDto;

import java.util.List;

public interface BudgetCommonRepository {
    List<BudgetDetailsDto> getBudgetsByUserId(Long userId, int month, int year, String category);
}
