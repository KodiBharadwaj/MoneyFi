package com.moneyfi.expense.repository;

import com.moneyfi.expense.model.ExpenseModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<ExpenseModel, Long> {
    @Query("SELECT e FROM ExpenseModel e WHERE e.userId = :userId")
    List<ExpenseModel> findExpensesByUserId(Long userId);

    @Query(nativeQuery = true, value = "exec getMonthlyExpensesListInAYear @userId = :userId, " +
            "@year = :year, @deleteStatus = :deleteStatus")
    List<Object[]> findMonthlyExpenses(Long userId, int year, boolean deleteStatus);

    @Query(nativeQuery = true, value = "exec getMonthlyIncomesListInAYear @userId = :userId, " +
            "@year = :year, @deleteStatus = :deleteStatus")
    List<Object[]> getMonthlyIncomesListInAYear(Long userId, int year, boolean deleteStatus);

    @Query(nativeQuery = true, value = "exec getTotalExpenseInMonthAndYear @userId = :userId, " +
            "@month = :month, @year = :year")
    BigDecimal getTotalExpenseInMonthAndYear(Long userId, int month, int year);

    @Query(nativeQuery = true, value = "exec getTotalIncomeInMonthAndYear @userId = :userId, " +
            "@month = :month, @year = :year")
    BigDecimal getTotalIncomeInMonthAndYear(Long userId, int month, int year);

    @Query(nativeQuery = true, value =  "exec getUserIdFromUsernameAndToken @username = :username, @token = :token")
    Long getUserIdFromUsernameAndToken(String username, String token);

    @Query("SELECT SUM(e.amount) FROM ExpenseModel e WHERE e.userId = :userId AND e.isDeleted = false AND e.date BETWEEN :fromDate AND :toDate")
    BigDecimal getTotalIncomeInSpecifiedRange(@Param("userId") Long userId, @Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);
}
