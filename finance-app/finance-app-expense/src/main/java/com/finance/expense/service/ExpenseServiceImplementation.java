package com.finance.expense.service;

import com.finance.expense.model.ExpenseModel;
import com.finance.expense.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
public class ExpenseServiceImplementation implements ExpenseService{

    @Autowired
    private ExpenseRepository expenseRepository;

    @Override
    public ExpenseModel save(ExpenseModel expense) {
        return expenseRepository.save(expense);
    }

    @Override
    public List<ExpenseModel> getAllexpenses(int userId) {
        return expenseRepository.findExpensesByUserId(userId);
    }

    @Override
    public List<ExpenseModel> getAllexpensesByDate(int userId, int month, int year) {
        return expenseRepository.getAllexpensesByDate(userId, month, year);
    }

    @Override
    public List<ExpenseModel> getAllexpensesByYear(int userId, int year) {
        return expenseRepository.getAllexpensesByYear(userId, year);
    }

    @Override
    public List<Double> getMonthlyExpenses(int userId, int year) {
        List<Object[]> rawExpenses = expenseRepository.findMonthlyExpenses(userId, year);
        Double[] monthlyTotals = new Double[12];
        Arrays.fill(monthlyTotals, 0.0); // Initialize all months to 0

        for (Object[] raw : rawExpenses) {
            int month = ((Integer) raw[0]) - 1; // Months are 1-based, array is 0-based
            double total = (Double) raw[1];
            monthlyTotals[month] = total;
        }

        return Arrays.asList(monthlyTotals);
    }

    @Override
    public ExpenseModel updateBySource(int id, ExpenseModel expense) {
        ExpenseModel expenseModel = expenseRepository.findById(id).orElse(null);

        if(expense.getCategory() != null){
            expenseModel.setCategory(expense.getCategory());
        }
        if(expense.getAmount() > 0){
            expenseModel.setAmount(expense.getAmount());
        }
        if(expense.getDate() != null){
            expenseModel.setDate(expense.getDate());
        }
        if(expense.getDescription() != null){
            expenseModel.setDescription(expense.getDescription());
        }
        if(expense.isRecurring()){
            expenseModel.setRecurring(expense.isRecurring());
        }

        return save(expenseModel);
    }

    @Override
    public void deleteExpenseById(int id) {
        expenseRepository.deleteById(id);
    }
}
