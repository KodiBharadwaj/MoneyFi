package com.finance.expense.repository;


import com.finance.expense.dto.ExpenseDto;
import com.finance.expense.model.ExpenseModel;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<ExpenseModel, Integer> {

    @Query("select e from ExpenseModel e where e.userId = :userId")
    public List<ExpenseModel> findExpensesByUserId(int userId);

    @Query("select e from ExpenseModel e where e.userId=:userId and e.category=:category")
    public ExpenseModel findByUserIdAndCatergory(int userId, String category);

    @Query("SELECT e FROM ExpenseModel e WHERE e.userId = :userId " +
            "AND EXTRACT(MONTH FROM e.date) = :month " +
            "AND EXTRACT(YEAR FROM e.date) = :year " +
            "AND e.is_deleted = :deleteStatus")
    public List<ExpenseModel> getAllexpensesByDate(int userId, int month, int year, boolean deleteStatus);

    @Query("SELECT e FROM ExpenseModel e WHERE e.userId = :userId AND e.is_deleted = :deleteStatus " +
            "AND EXTRACT(YEAR FROM e.date) = :year")
    public List<ExpenseModel> getAllexpensesByYear(int userId, int year, boolean deleteStatus);

//    @Query("SELECT MONTH(e.date) AS month, SUM(e.amount) AS total " +
//            "FROM ExpenseModel e " +
//            "where e.userId = :userId AND YEAR(e.date)=:year AND e.is_deleted = :deleteStatus " +
//            "GROUP BY MONTH(e.date) " +
//            "ORDER BY month ASC")
    @Query(nativeQuery = true, value = "exec findMonthlyExpensesListInAYear @userId = :userId, " +
            "@year = :year, @deleteStatus = :deleteStatus")
    public List<Object[]> findMonthlyExpenses(int userId, int year, boolean deleteStatus);

    @Query(nativeQuery = true, value = "exec getTotalExpensesUpToPreviousMonth " +
        "@userId = :userId, @month = :month, @year = :year")
    public Double getTotalExpensesUpToPreviousMonth(int userId, int month, int year);

    @Query(nativeQuery = true, value = "exec getTotalExpenseInMonthAndYear @userId = :userId, " +
            "@month = :month, @year = :year")
    public Double getTotalExpenseInMonthAndYear(int userId, int month, int year);


}
