package com.moneyfi.expense.repository;

import com.moneyfi.expense.model.ExpenseModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<ExpenseModel, Long> {

    @Query(nativeQuery = true, value = "exec getAllExpensesByUserId @userId = :userId")
    List<ExpenseModel> findExpensesByUserId(Long userId);


    @Query(nativeQuery = true, value = "exec getAllExpensesByMonthAndYear @userId = :userId, " +
            "@month = :month, @year = :year, @deleteStatus = :deleteStatus")
    List<ExpenseModel> getAllExpensesByDate(Long userId, int month, int year, boolean deleteStatus);


    @Query(nativeQuery = true, value = "exec getAllExpensesByYear @userId = :userId, " +
            "@year = :year, @deleteStatus = :deleteStatus")
    List<ExpenseModel> getAllExpensesByYear(Long userId, int year, boolean deleteStatus);


    @Query(nativeQuery = true, value = "exec getMonthlyExpensesListInAYear @userId = :userId, " +
            "@year = :year, @deleteStatus = :deleteStatus")
    List<Object[]> findMonthlyExpenses(Long userId, int year, boolean deleteStatus);


    @Query(nativeQuery = true, value = "exec getTotalExpensesUpToPreviousMonth " +
            "@userId = :userId, @month = :month, @year = :year")
    BigDecimal getTotalExpensesUpToPreviousMonth(Long userId, int month, int year);


    @Query(nativeQuery = true, value = "exec getTotalExpenseInMonthAndYear @userId = :userId, " +
            "@month = :month, @year = :year")
    BigDecimal getTotalExpenseInMonthAndYear(Long userId, int month, int year);

}
