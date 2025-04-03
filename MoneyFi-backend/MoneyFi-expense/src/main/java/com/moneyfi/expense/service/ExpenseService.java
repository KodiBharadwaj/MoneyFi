package com.moneyfi.expense.service;

import com.moneyfi.expense.model.ExpenseModel;

import java.util.List;

public interface ExpenseService {

    public ExpenseModel save(ExpenseModel expense);

    public List<ExpenseModel> getAllexpenses(int userId);

    public List<ExpenseModel> getAllexpensesByDate(int userId, int month, int year, boolean deleteStatus);

    public byte[] generateMonthlyExcelReport(int userId, int month, int year);

    public List<ExpenseModel> getAllexpensesByYear(int userId, int year, boolean deleteStatus);

    public byte[] generateYearlyExcelReport(int userId, int year);

    public List<Double> getMonthlyExpenses(int userId, int year);

    public Double getTotalExpensesUpToPreviousMonth(int userId, int month, int year);

    public Double getTotalExpenseInMonthAndYear(int userId, int month, int year);

    public Double getTotalSavingsByMonthAndDate(int userId, int month, int year);

    public List<Double> getCumulativeMonthlySavings(int userId, int year);

    public ExpenseModel updateBySource(int id, ExpenseModel expense);

    public boolean deleteExpenseById(int id);

}
