package com.moneyfi.income.service;

import com.moneyfi.income.model.IncomeModel;

import java.math.BigDecimal;
import java.util.List;

public interface IncomeService {

    IncomeModel save(IncomeModel income);

    List<IncomeModel> getAllIncomes(Long userId);

    List<IncomeModel> getAllIncomesByDate(Long userId, int month, int year, boolean deleteStatus);

    byte[] generateMonthlyExcelReport(Long userId, int month, int year);

    List<IncomeModel> getAllIncomesByYear(Long userId, int year, boolean deleteStatus);

    byte[] generateYearlyExcelReport(Long userId, int year);

    List<BigDecimal> getMonthlyIncomes(Long userId, int year);

    BigDecimal getTotalIncomeInMonthAndYear(Long userId, int month, int year);

    BigDecimal getRemainingIncomeUpToPreviousMonthByMonthAndYear(Long userId, int month, int year);

    boolean incomeUpdateCheckFunction(IncomeModel incomeModel);

    boolean incomeDeleteCheckFunction(IncomeModel incomeModel);

    IncomeModel updateBySource(Long id, IncomeModel income);

    boolean deleteIncomeById(Long id);

}
