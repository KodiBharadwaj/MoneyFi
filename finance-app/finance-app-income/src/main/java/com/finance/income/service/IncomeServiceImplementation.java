package com.finance.income.service;

import com.finance.income.model.IncomeModel;
import com.finance.income.repository.IncomeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class IncomeServiceImplementation implements IncomeService {

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private RestTemplate restTemplate;

    boolean flag = false;

    @Override
    public IncomeModel save(IncomeModel income) {
         if(flag == false){
             IncomeModel incomeModel = incomeRepository.getIncomeBySourceAndCategory(income.getUserId(), income.getSource(), income.getCategory());

             if(incomeModel != null){
                 return null;
             }
             income.set_deleted(false);
             return incomeRepository.save(income);
         }
         else {
             income.set_deleted(false);
             return incomeRepository.save(income);
         }
    }

    @Override
    public List<IncomeModel> getAllIncomes(int userId) {
        return incomeRepository.findIncomesOfUser(userId)
                .stream()
                .filter(i->i.is_deleted()==false)
                .toList();
    }

    @Override
    public List<IncomeModel> getAllIncomesByDate(int userId, int month, int year, boolean deleteStatus) {
        return incomeRepository.getAllIncomesByDate(userId, month, year, deleteStatus);
    }

    @Override
    public List<IncomeModel> getAllIncomesByYear(int userId, int year, boolean deleteStatus) {
        return incomeRepository.getAllIncomesByYear(userId, year, deleteStatus);
    }

    @Override
    public List<Double> getMonthlyIncomes(int userId, int year) {
        List<Object[]> rawIncomes = incomeRepository.findMonthlyIncomes(userId, year, false);
        Double[] monthlyTotals = new Double[12];
        Arrays.fill(monthlyTotals, 0.0); // Initialize all months to 0

        for (Object[] raw : rawIncomes) {
            int month = ((Integer) raw[0]) - 1; // Months are 1-based, array is 0-based
            double total = (Double) raw[1];
            monthlyTotals[month] = total;
        }

        return Arrays.asList(monthlyTotals);
    }

    @Override
    public Double getTotalIncomeInMonthAndYear(int userId, int month, int year) {
        return incomeRepository.getTotalIncomeInMonthAndYear(userId, month, year);
    }

    @Override
    public Double getRemainingIncomeUpToPreviousMonthByMonthAndYear(int userId, int month, int year) {

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

        Double totalIncome = incomeRepository.getRemainingIncomeUpToPreviousMonthByMonthAndYear(userId, adjustedMonth, adjustedYear);
        if(totalIncome == null || totalIncome == 0){
            return 0.0;
        }
        Double totalExpense = restTemplate.getForObject("http://FINANCE-APP-EXPENSE/api/expense/" + userId + "/totalExpensesUpToPreviousMonth/" + month +"/" + year, Double.class);
        if(totalExpense > totalIncome){
            return 0.0;
        }
        return (totalIncome - totalExpense);
    }

    @Override
    public boolean incomeUpdateCheckFunction(IncomeModel incomeModel) {

        Double totalIncome = getTotalIncomeInMonthAndYear(incomeModel.getUserId(), incomeModel.getDate().getMonthValue(), incomeModel.getDate().getYear());
        Double previousUpdatedIncome = incomeRepository.getIncomeByIncomeId(incomeModel.getId());
        Double updatedIncome = incomeModel.getAmount();
        Double currentNetIncome = totalIncome - previousUpdatedIncome + updatedIncome;
        Double totalExpensesInMonth = restTemplate.getForObject("http://FINANCE-APP-USER/api/user/expenses/" + incomeModel.getUserId() + "/totalExpenses/" + incomeModel.getDate().getMonthValue() + "/" + incomeModel.getDate().getYear(), Double.class);

        if(currentNetIncome > totalExpensesInMonth){
            return true;
        }
        return false;
    }

    @Override
    public boolean incomeDeleteCheckFunction(IncomeModel incomeModel) {

        Double totalIncome = getTotalIncomeInMonthAndYear(incomeModel.getUserId(), incomeModel.getDate().getMonthValue(), incomeModel.getDate().getYear());
        Double previousUpdatedIncome = incomeRepository.getIncomeByIncomeId(incomeModel.getId());
        Double updatedIncome = 0.0;
        Double currentNetIncome = totalIncome - previousUpdatedIncome + updatedIncome;
        Double totalExpensesInMonth = restTemplate.getForObject("http://FINANCE-APP-USER/api/user/expenses/" + incomeModel.getUserId() + "/totalExpenses/" + incomeModel.getDate().getMonthValue() + "/" + incomeModel.getDate().getYear(), Double.class);

        if(currentNetIncome > totalExpensesInMonth){
            return true;
        }

        return false;
    }

    @Override
    public IncomeModel updateBySource(int id, IncomeModel income) {

        flag = true;
        IncomeModel incomeModel = incomeRepository.findById(id).orElse(null);

        if(income.getAmount() > 0){
            incomeModel.setAmount(income.getAmount());
        }
        if(income.getSource() != null){
            incomeModel.setSource(income.getSource());
        }
        if(income.getCategory() != null){
            incomeModel.setCategory(income.getCategory());
        }
        if(income.getDate() != null){
            incomeModel.setDate(income.getDate());
        }
        incomeModel.setRecurring(income.isRecurring());

        return save(incomeModel);
    }

    @Override
    public boolean deleteIncomeById(int id) {

        try {
            IncomeModel income = incomeRepository.findById(id).orElse(null);
            income.set_deleted(true);
            incomeRepository.save(income);
            return true;

        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }

}
