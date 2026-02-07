package com.moneyfi.transaction.repository.expense;

import com.moneyfi.transaction.model.expense.ExpenseModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
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
            value = "SELECT clt.category, " +
                    "CAST(SUM(CAST(et.amount AS DECIMAL(18,2))) AS DECIMAL(18,2)) AS totalAmount " +
                    "FROM expense_table et WITH (NOLOCK) " +
                    "INNER JOIN category_list_table clt WITH (NOLOCK) ON clt.id = et.category_id " +
                    "WHERE et.user_id = :userId " +
                    "AND et.is_deleted = 0 " +
                    "AND et.date BETWEEN :fromDate AND :toDate " +
                    "GROUP BY clt.category",
            nativeQuery = true
    )
    List<Object[]> getTotalIncomeInSpecifiedRange(@Param("userId") Long userId, @Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);

    @Query(
            value = "SELECT et.* " +
                    "FROM expense_table et WITH (NOLOCK) " +
                    "WHERE et.user_id = :userId " +
                    "AND et.is_deleted = 0 " +
                    "AND et.entry_mode = 'GMAIL_SYNC' " +
                    "AND CAST(et.gmail_sync_date AS DATE) = :date ",
            nativeQuery = true)
    List<ExpenseModel> getGmailSyncAddedExpenses(Long userId, LocalDate date);
}
