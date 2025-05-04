package com.finance.user.service.microservices.expense;

import com.finance.user.dto.BudgetModel;
import com.finance.user.dto.ExpenseModel;
import com.finance.user.feign.BudgetFeignClient;
import com.finance.user.feign.ExpenseFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class UserExpenseServiceImplementation implements UserExpenseService {

    @Autowired
    private ExpenseFeignClient expenseFeignClient;

    @Autowired
    private BudgetFeignClient budgetFeignClient;


    @Override
    public ExpenseModel addExpense(int userId, ExpenseModel expense) {
        expense.setUserId(userId);
        ExpenseModel expenseResponse = expenseFeignClient.saveExpense(expense).getBody();
        return expenseResponse;
    }

    @Override
    public List<ExpenseModel> getAllExpenses(int userId) {
        return expenseFeignClient.getAllExpenses(userId).getBody();
    }

    @Override
    public List<ExpenseModel> getAllExpensesByDate(int userId, int month, int year) {
        return expenseFeignClient.getAllExpensesByDate(userId, month,year).getBody();
    }

    @Override
    public List<ExpenseModel> getAllExpensesByYear(int userId, int year) {
        return expenseFeignClient.getAllExpensesByYear(userId, year).getBody();
    }

    @Override
    public ExpenseModel updateExpense(int id, ExpenseModel expense) {
//        expense.setUserId(userId);
        return expenseFeignClient.updateExpense(id, expense);
//        return expenseFeignClient.getAllExpenses(expense.getUserId()).getBody();
    }

    @Override
    public boolean deleteExpenseById(int id) {

        try {
            expenseFeignClient.deleteExpenseById(id);
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }
}
