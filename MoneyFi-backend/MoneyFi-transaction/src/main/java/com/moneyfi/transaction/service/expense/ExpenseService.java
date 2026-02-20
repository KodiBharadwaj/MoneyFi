package com.moneyfi.transaction.service.expense;

import com.moneyfi.transaction.model.expense.ExpenseModel;
import com.moneyfi.transaction.service.expense.dto.response.ExpenseDetailsDto;
import com.moneyfi.transaction.service.income.dto.request.TransactionsListRequestDto;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ExpenseService {

    ExpenseModel save(ExpenseModel expense);

    List<ExpenseModel> getAllExpenses(Long userId);

    List<ExpenseDetailsDto> getAllExpensesByDate(Long userId, TransactionsListRequestDto requestDto);

    byte[] getExpenseReportExcel(Long userId, TransactionsListRequestDto requestDto);

    List<BigDecimal> getMonthlyExpenses(Long userId, int year);

    List<BigDecimal> getMonthlySavingsList(Long userId, int year);

    BigDecimal getTotalExpenseInMonthAndYear(Long userId, int month, int year);

    BigDecimal getTotalSavingsByMonthAndDate(Long userId, int month, int year);

    List<BigDecimal> getCumulativeMonthlySavings(Long userId, int year);

    ResponseEntity<ExpenseDetailsDto> updateBySource(Long id, Long userId, ExpenseModel expense);

    boolean deleteExpenseById(List<Long> ids);

    List<Object[]> getTotalExpensesInSpecifiedRange(Long userId, LocalDateTime fromDate, LocalDateTime toDate);
}
