package com.moneyfi.expense.repository;

import com.moneyfi.expense.model.ExpenseModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<ExpenseModel, Long> {

    @Query(nativeQuery = true, value = "exec getAllExpensesByUserId @userId = :userId")
    List<ExpenseModel> findExpensesByUserId(Long userId);


    @Query(nativeQuery = true, value = "exec getMonthlyExpensesListInAYear @userId = :userId, " +
            "@year = :year, @deleteStatus = :deleteStatus")
    List<Object[]> findMonthlyExpenses(Long userId, int year, boolean deleteStatus);


    @Query(nativeQuery = true, value = "exec getMonthlyIncomesListInAYear @userId = :userId, " +
            "@year = :year, @deleteStatus = :deleteStatus")
    List<Object[]> getMonthlyIncomesListInAYear(Long userId, int year, boolean deleteStatus);

}
