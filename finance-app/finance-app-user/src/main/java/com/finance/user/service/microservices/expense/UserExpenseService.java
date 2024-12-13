package com.finance.user.service.microservices.expense;

import com.finance.user.dto.ExpenseModel;
import com.finance.user.dto.IncomeModel;

import java.util.List;

public interface UserExpenseService {
    public ExpenseModel addExpense(int userId, ExpenseModel expense);

    public List<ExpenseModel> getAllExpenses(int userId);

    public List<ExpenseModel> getAllExpensesByDate(int userId, int month, int year);

    public List<ExpenseModel> getAllExpensesByYear(int userId, int year);

    public ExpenseModel updateExpense(int id, ExpenseModel expense);

    public boolean deleteExpenseById(int id);
}
