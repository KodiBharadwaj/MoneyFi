package com.moneyfi.income.service;

import com.moneyfi.income.model.IncomeModel;

import java.util.List;

public interface IncomeService {

    public IncomeModel save(IncomeModel income);

    List<IncomeModel> getAllIncomes(int userId);

    public List<IncomeModel> getAllIncomesByDate(int userId, int month, int year, boolean deleteStatus);

    public byte[] generateMonthlyExcelReport(int userId, int month, int year);

    public List<IncomeModel> getAllIncomesByYear(int userId, int year, boolean deleteStatus);

    public byte[] generateYearlyExcelReport(int userId, int year);

    public List<Double> getMonthlyIncomes(int userId, int year);

    public Double getTotalIncomeInMonthAndYear(int userId, int month, int year);

    public Double getRemainingIncomeUpToPreviousMonthByMonthAndYear(int userId, int month, int year);

    public boolean incomeUpdateCheckFunction(IncomeModel incomeModel);

    public boolean incomeDeleteCheckFunction(IncomeModel incomeModel);

    public IncomeModel updateBySource(int id, IncomeModel income);

    public boolean deleteIncomeById(int id);

}
