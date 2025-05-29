package com.moneyfi.expense.service;

import com.moneyfi.expense.model.ExpenseModel;
import com.moneyfi.expense.service.dto.response.ExpenseDetailsDto;

import java.math.BigDecimal;
import java.util.List;

public interface ExpenseService {

    ExpenseModel save(ExpenseModel expense);

    List<ExpenseModel> getAllExpenses(Long userId);

    List<ExpenseDetailsDto> getAllExpensesByMonthYearAndCategory(Long userId, int month, int year, String category, boolean deleteStatus);

    byte[] generateMonthlyExcelReport(Long userId, int month, int year, String category);

    List<ExpenseDetailsDto> getAllExpensesByYearAndCategory(Long userId, int year, String category, boolean deleteStatus);

    byte[] generateYearlyExcelReport(Long userId, int year, String category);

    List<BigDecimal> getMonthlyExpenses(Long userId, int year);

    List<BigDecimal> getMonthlySavingsList(Long userId, int year);

    BigDecimal getTotalExpenseInMonthAndYear(Long userId, int month, int year);

    BigDecimal getTotalSavingsByMonthAndDate(Long userId, int month, int year);

    List<BigDecimal> getCumulativeMonthlySavings(Long userId, int year);

    ExpenseDetailsDto updateBySource(Long id, Long userId, ExpenseModel expense);

    boolean deleteExpenseById(List<Long> ids);
}
