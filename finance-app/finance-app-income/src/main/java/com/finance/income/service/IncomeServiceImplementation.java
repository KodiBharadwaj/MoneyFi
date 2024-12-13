package com.finance.income.service;

import com.finance.income.model.IncomeModel;
import com.finance.income.repository.IncomeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class IncomeServiceImplementation implements IncomeService {

    @Autowired
    private IncomeRepository incomeRepository;

    @Override
    public IncomeModel save(IncomeModel income) {
        return incomeRepository.save(income);
    }

    @Override
    public List<IncomeModel> getAllIncomes(int userId) {
        return incomeRepository.findIncomesOfUser(userId);
    }

    @Override
    public List<IncomeModel> getAllIncomesByDate(int userId, int month, int year) {
        return incomeRepository.getAllIncomesByDate(userId, month, year);
    }

    @Override
    public List<IncomeModel> getAllIncomesByYear(int userId, int year) {
        return incomeRepository.getAllIncomesByYear(userId, year);
    }

    @Override
    public List<Double> getMonthlyIncomes(int userId, int year) {
        List<Object[]> rawIncomes = incomeRepository.findMonthlyIncomes(userId, year);
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
    public IncomeModel updateBySource(int id, IncomeModel income) {

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
    public void deleteParticularIncomeBySource(int id) {
        incomeRepository.deleteById(id);
    }
}
