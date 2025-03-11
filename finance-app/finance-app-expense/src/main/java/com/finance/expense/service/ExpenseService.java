package com.finance.expense.service;

import com.finance.expense.model.ExpenseModel;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseService {

    public ExpenseModel save(ExpenseModel expense);

    public List<ExpenseModel> getAllexpenses(int userId);

    public List<ExpenseModel> getAllexpensesByDate(int userId, int month, int year, boolean deleteStatus);

    public List<ExpenseModel> getAllexpensesByYear(int userId, int year, boolean deleteStatus);

    public List<Double> getMonthlyExpenses(int userId, int year);

    public Double getTotalExpensesUpToPreviousMonth(int userId, int month, int year);

    public ExpenseModel updateBySource(int id, ExpenseModel expense);

    public void deleteExpenseById(int id);

}
