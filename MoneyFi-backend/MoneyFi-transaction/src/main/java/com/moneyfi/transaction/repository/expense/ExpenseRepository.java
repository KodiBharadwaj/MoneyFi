package com.moneyfi.transaction.repository.expense;

import com.moneyfi.transaction.model.expense.ExpenseModel;
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

    @Query(
            value = "SELECT e.category, " +
                    "CAST(SUM(CAST(e.amount AS DECIMAL(18,2))) AS DECIMAL(18,2)) AS totalAmount " +
                    "FROM expense_table e " +
                    "WHERE e.user_id = :userId " +
                    "AND e.is_deleted = 0 " +
                    "AND e.date BETWEEN :fromDate AND :toDate " +
                    "GROUP BY e.category",
            nativeQuery = true
    )
    List<Object[]> getTotalIncomeInSpecifiedRange(@Param("userId") Long userId, @Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);
}
