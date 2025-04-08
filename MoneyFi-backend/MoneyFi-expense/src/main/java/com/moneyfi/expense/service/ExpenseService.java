package com.moneyfi.expense.service;

import com.moneyfi.expense.model.ExpenseModel;

import java.math.BigDecimal;
import java.util.List;

public interface ExpenseService {

    ExpenseModel save(ExpenseModel expense);

    List<ExpenseModel> getAllExpenses(Long userId);

    List<ExpenseModel> getAllExpensesByMonthYearAndCategory(Long userId, int month, int year, String category, boolean deleteStatus);

    byte[] generateMonthlyExcelReport(Long userId, int month, int year, String category);

    List<ExpenseModel> getAllExpensesByYearAndCategory(Long userId, int year, String category, boolean deleteStatus);

    byte[] generateYearlyExcelReport(Long userId, int year, String category);

    List<BigDecimal> getMonthlyExpenses(Long userId, int year);

    BigDecimal getTotalExpensesUpToPreviousMonth(Long userId, int month, int year);

    BigDecimal getTotalExpenseInMonthAndYear(Long userId, int month, int year);

    BigDecimal getTotalSavingsByMonthAndDate(Long userId, int month, int year);

    List<BigDecimal> getCumulativeMonthlySavings(Long userId, int year);

    ExpenseModel updateBySource(Long id, ExpenseModel expense);

    boolean deleteExpenseById(Long id);

}
