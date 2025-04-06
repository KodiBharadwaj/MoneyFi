package com.moneyfi.expense.service;

import com.moneyfi.expense.model.ExpenseModel;

import java.math.BigDecimal;
import java.util.List;

public interface ExpenseService {

    ExpenseModel save(ExpenseModel expense);

    List<ExpenseModel> getAllexpenses(Long userId);

    List<ExpenseModel> getAllexpensesByDate(Long userId, int month, int year, boolean deleteStatus);

    byte[] generateMonthlyExcelReport(Long userId, int month, int year);

    List<ExpenseModel> getAllexpensesByYear(Long userId, int year, boolean deleteStatus);

    byte[] generateYearlyExcelReport(Long userId, int year);

    List<BigDecimal> getMonthlyExpenses(Long userId, int year);

    BigDecimal getTotalExpensesUpToPreviousMonth(Long userId, int month, int year);

    BigDecimal getTotalExpenseInMonthAndYear(Long userId, int month, int year);

    BigDecimal getTotalSavingsByMonthAndDate(Long userId, int month, int year);

    List<BigDecimal> getCumulativeMonthlySavings(Long userId, int year);

    ExpenseModel updateBySource(Long id, ExpenseModel expense);

    boolean deleteExpenseById(Long id);

}
