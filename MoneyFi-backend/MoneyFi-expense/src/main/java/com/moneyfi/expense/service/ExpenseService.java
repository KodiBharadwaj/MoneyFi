package com.moneyfi.expense.service;

import com.moneyfi.expense.model.ExpenseModel;

import java.util.List;

public interface ExpenseService {

    public ExpenseModel save(ExpenseModel expense);

    public List<ExpenseModel> getAllexpenses(Long userId);

    public List<ExpenseModel> getAllexpensesByDate(Long userId, int month, int year, boolean deleteStatus);

    public byte[] generateMonthlyExcelReport(Long userId, int month, int year);

    public List<ExpenseModel> getAllexpensesByYear(Long userId, int year, boolean deleteStatus);

    public byte[] generateYearlyExcelReport(Long userId, int year);

    public List<Double> getMonthlyExpenses(Long userId, int year);

    public Double getTotalExpensesUpToPreviousMonth(Long userId, int month, int year);

    public Double getTotalExpenseInMonthAndYear(Long userId, int month, int year);

    public Double getTotalSavingsByMonthAndDate(Long userId, int month, int year);

    public List<Double> getCumulativeMonthlySavings(Long userId, int year);

    public ExpenseModel updateBySource(Long id, ExpenseModel expense);

    public boolean deleteExpenseById(Long id);

}
