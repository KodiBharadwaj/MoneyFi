package com.finance.income.service;

import com.finance.income.model.IncomeModel;

import java.util.List;

public interface IncomeService {

    public IncomeModel save(IncomeModel income);

    List<IncomeModel> getAllIncomes(int userId);

    public List<IncomeModel> getAllIncomesByDate(int userId, int month, int year, boolean deleteStatus);

    public List<IncomeModel> getAllIncomesByYear(int userId, int year, boolean deleteStatus);

    public List<Double> getMonthlyIncomes(int userId, int year);

    public Double getTotalIncomeInMonthAndYear(int userId, int month, int year);

    public Double getRemainingIncomeUpToPreviousMonthByMonthAndYear(int userId, int month, int year);

    public boolean incomeUpdateCheckFunction(IncomeModel incomeModel);

    public boolean incomeDeleteCheckFunction(IncomeModel incomeModel);

    public IncomeModel updateBySource(int id, IncomeModel income);

    public boolean deleteIncomeById(int id);

}
