package com.moneyfi.income.repository.common;

import com.moneyfi.income.service.dto.response.IncomeDeletedDto;
import com.moneyfi.income.service.dto.response.IncomeDetailsDto;

import java.math.BigDecimal;
import java.util.List;

public interface IncomeCommonRepository {

    List<IncomeDetailsDto> getAllIncomesByDate(Long userId, int month, int year, String category, boolean deleteStatus);

    List<IncomeDeletedDto> getDeletedIncomesInAMonth(Long userId, int month, int year);

    List<IncomeDetailsDto> getAllIncomesByYear(Long userId, int year, String category, boolean deleteStatus);

    BigDecimal getTotalIncomeInMonthAndYear(Long userId, int month, int year);

    BigDecimal getRemainingIncomeUpToPreviousMonthByMonthAndYear(Long userId, int adjustedMonth, int adjustedYear);

    BigDecimal getTotalExpensesUpToPreviousMonth(Long userId, int adjustedMonth, int adjustedYear);

    BigDecimal getTotalExpenseInMonthAndYear(Long userId, int month, int year);

    BigDecimal getAvailableBalanceOfUser(Long userId);
}
