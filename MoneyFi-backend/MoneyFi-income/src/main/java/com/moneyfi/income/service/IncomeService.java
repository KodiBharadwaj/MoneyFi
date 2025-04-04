package com.moneyfi.income.service;

import com.moneyfi.income.model.IncomeModel;

import java.util.List;

public interface IncomeService {

    public IncomeModel save(IncomeModel income);

    List<IncomeModel> getAllIncomes(Long userId);

    public List<IncomeModel> getAllIncomesByDate(Long userId, int month, int year, boolean deleteStatus);

    public byte[] generateMonthlyExcelReport(Long userId, int month, int year);

    public List<IncomeModel> getAllIncomesByYear(Long userId, int year, boolean deleteStatus);

    public byte[] generateYearlyExcelReport(Long userId, int year);

    public List<Double> getMonthlyIncomes(Long userId, int year);

    public Double getTotalIncomeInMonthAndYear(Long userId, int month, int year);

    public Double getRemainingIncomeUpToPreviousMonthByMonthAndYear(Long userId, int month, int year);

    public boolean incomeUpdateCheckFunction(IncomeModel incomeModel);

    public boolean incomeDeleteCheckFunction(IncomeModel incomeModel);

    public IncomeModel updateBySource(Long id, IncomeModel income);

    public boolean deleteIncomeById(Long id);

}
