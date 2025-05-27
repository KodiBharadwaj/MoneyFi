package com.moneyfi.expense.repository.common;

import com.moneyfi.expense.service.dto.response.ExpenseDetailsDto;

import java.math.BigDecimal;
import java.util.List;

public interface ExpenseCommonRepository {
    List<ExpenseDetailsDto> getAllExpensesByDate(Long userId, int month, int year, String category, boolean deleteStatus);

    List<ExpenseDetailsDto> getAllExpensesByYear(Long userId, int year, String category, boolean deleteStatus);

    BigDecimal getTotalExpenseInMonthAndYear(Long userId, int month, int year);

    BigDecimal getTotalIncomeInMonthAndYear(Long userId, int month, int year);
}
