package com.moneyfi.transaction.service.expense;

import com.moneyfi.transaction.model.expense.ExpenseModel;
import com.moneyfi.transaction.service.expense.dto.response.ExpenseDetailsDto;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ExpenseService {

    ExpenseModel save(ExpenseModel expense);

    List<ExpenseModel> getAllExpenses(Long userId);

    List<ExpenseDetailsDto> getAllExpensesByMonthYearAndCategory(Long userId, int month, int year, String category, boolean deleteStatus);

    byte[] generateMonthlyExcelReport(Long userId, int month, int year, String category);

    List<ExpenseDetailsDto> getAllExpensesByYearAndCategory(Long userId, int year, String category, boolean deleteStatus);

    byte[] generateYearlyExcelReport(Long userId, int year, String category);

    List<BigDecimal> getMonthlyExpenses(Long userId, int year);

    List<BigDecimal> getMonthlySavingsList(Long userId, int year);

    BigDecimal getTotalExpenseInMonthAndYear(Long userId, int month, int year);

    BigDecimal getTotalSavingsByMonthAndDate(Long userId, int month, int year);

    List<BigDecimal> getCumulativeMonthlySavings(Long userId, int year);

    ResponseEntity<ExpenseDetailsDto> updateBySource(Long id, Long userId, ExpenseModel expense);

    boolean deleteExpenseById(List<Long> ids);

    List<Object[]> getTotalExpensesInSpecifiedRange(Long userId, LocalDateTime fromDate, LocalDateTime toDate);
}
