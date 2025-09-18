package com.moneyfi.income.repository;

import com.moneyfi.income.model.IncomeModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface IncomeRepository extends JpaRepository<IncomeModel, Long> {

    @Query("SELECT i FROM IncomeModel i WHERE i.userId = :userId")
    List<IncomeModel> findIncomesOfUser(Long userId);

    @Query(nativeQuery = true, value = "exec getMonthlyIncomesListInAYear @userId = :userId, " +
            "@year = :year, @deleteStatus = :deleteStatus")
    List<Object[]> findMonthlyIncomes(Long userId, int year, boolean deleteStatus);

    @Query(nativeQuery = true, value = "exec getIncomeBySourceAndCategory @userId = :userId, " +
            "@source = :source, @category = :category, @date = :date")
    IncomeModel getIncomeBySourceAndCategory(Long userId, String source, String category, LocalDateTime date);

    @Query("SELECT i.amount FROM IncomeModel i WHERE i.id = :incomeId")
    BigDecimal getIncomeByIncomeId(Long incomeId);

    @Query(nativeQuery = true, value = "exec getTotalIncomeInMonthAndYear @userId = :userId, " +
            "@month = :month, @year = :year")
    BigDecimal getTotalIncomeInMonthAndYear(Long userId, int month, int year);

    @Query(nativeQuery = true, value = "exec getTotalExpenseInMonthAndYear @userId = :userId, " +
            "@month = :month, @year = :year")
    BigDecimal getTotalExpenseInMonthAndYear(Long userId, int month, int year);

    @Query(nativeQuery = true, value = "exec getAvailableBalanceOfUser @userId = :userId")
    BigDecimal getAvailableBalanceOfUser(Long userId);

    @Query(nativeQuery = true, value =  "exec getUserIdFromUsernameAndToken @username = :username, @token = :token")
    Long getUserIdFromUsernameAndToken(String username, String token);
}
