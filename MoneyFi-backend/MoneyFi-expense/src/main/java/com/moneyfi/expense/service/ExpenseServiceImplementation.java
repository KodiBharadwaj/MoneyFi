package com.moneyfi.expense.service;

import com.moneyfi.expense.model.ExpenseModel;
import com.moneyfi.expense.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ExpenseServiceImplementation implements ExpenseService{

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public ExpenseModel save(ExpenseModel expense) {
        expense.set_deleted(false);
        return expenseRepository.save(expense);
    }

    @Override
    public List<ExpenseModel> getAllexpenses(int userId) {
        return expenseRepository.findExpensesByUserId(userId).stream().filter(i->i.is_deleted() == false).toList();
    }

    @Override
    public List<ExpenseModel> getAllexpensesByDate(int userId, int month, int year, boolean deleteStatus) {
        return expenseRepository.getAllexpensesByDate(userId, month, year, deleteStatus);
    }

    @Override
    public List<ExpenseModel> getAllexpensesByYear(int userId, int year, boolean deleteStatus) {
        return expenseRepository.getAllexpensesByYear(userId, year, deleteStatus);
    }

    @Override
    public List<Double> getMonthlyExpenses(int userId, int year) {
        List<Object[]> rawExpenses = expenseRepository.findMonthlyExpenses(userId, year, false);
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
    public Double getTotalExpensesUpToPreviousMonth(int userId, int month, int year) {
        // Adjust month and year to point to the previous month
        final int adjustedMonth;
        final int adjustedYear;

        if (month == 1) { // Handle January case
            adjustedMonth = 13;
            adjustedYear = year - 1;
        } else {
            adjustedMonth = month;
            adjustedYear = year;
        }
        Double value = expenseRepository.getTotalExpensesUpToPreviousMonth(userId, adjustedMonth, adjustedYear);
        if(value != null){
            return value;
        } else {
            return 0.0;
        }
    }

    @Override
    public Double getTotalExpenseInMonthAndYear(int userId, int month, int year) {
        Double totalExpense = expenseRepository.getTotalExpenseInMonthAndYear(userId, month, year);
        if(totalExpense == null) return 0.0;

        return totalExpense;
    }

    @Override
    public Double getTotalSavingsByMonthAndDate(int userId, int month, int year) {
        Double totalIncome = restTemplate.getForObject("http://MONEYFI-INCOME/api/income/" + userId + "/totalIncome/" + month + "/" + year, Double.class);
        Double totalExpenses = getTotalExpenseInMonthAndYear(userId, month, year);
        if(totalIncome > totalExpenses){
            return (totalIncome - totalExpenses);
        }

        return 0.0;
    }

    @Override
    public List<Double> getCumulativeMonthlySavings(int userId, int year) {

        Double[] incomes = restTemplate.getForObject("http://MONEYFI-INCOME/api/income/"+userId+"/monthlyTotalIncomesList/"+year,Double[].class);
        Double[] expenses = getMonthlyExpenses(userId, year).toArray(new Double[0]);
        LocalDate currentDate = LocalDate.now();
        int currentYear = currentDate.getYear();
        int currentMonth = currentDate.getMonthValue();

        if(year > currentYear) return Arrays.asList(new Double[12]);

        int lastMonth = (year < currentYear) ? 12 : currentMonth;

        List<Double> savings = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            if(i < lastMonth){
                savings.add(incomes[i] - expenses[i]);
            }
            else{
                savings.add(0.0);
            }
        }

        List<Double> cumulativeSavings = new ArrayList<>();
        cumulativeSavings.add(savings.get(0));
        for(int i=1; i<12; i++){
            if(i < lastMonth){
                cumulativeSavings.add(cumulativeSavings.get(i-1)+savings.get(i));
            }
            else {
                cumulativeSavings.add(0.0);
            }
        }
        return cumulativeSavings;
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
    public boolean deleteExpenseById(int id) {

        try {
            ExpenseModel expense = expenseRepository.findById(id).orElse(null);
            expense.set_deleted(true);
            expenseRepository.save(expense);
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }

    }
}
