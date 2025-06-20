package com.moneyfi.income.repository;

import com.moneyfi.income.model.IncomeModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface IncomeRepository extends JpaRepository<IncomeModel, Long> {

    @Query(nativeQuery = true, value = "exec getAllIncomesOfUser @userId = :userId")
    List<IncomeModel> findIncomesOfUser(Long userId);


    @Query(nativeQuery = true, value = "exec getMonthlyIncomesListInAYear @userId = :userId, " +
            "@year = :year, @deleteStatus = :deleteStatus")
    List<Object[]> findMonthlyIncomes(Long userId, int year, boolean deleteStatus);


    @Query(nativeQuery = true, value = "exec getIncomeBySourceAndCategory @userId = :userId, " +
            "@source = :source, @category = :category, @date = :date")
    IncomeModel getIncomeBySourceAndCategory(Long userId, String source, String category, LocalDate date);


    @Query(nativeQuery = true, value = "exec getIncomeByIncomeId @incomeId = :incomeId")
    BigDecimal getIncomeByIncomeId(Long incomeId);

    @Query(nativeQuery = true, value = "exec getTotalIncomeInMonthAndYear @userId = :userId, " +
            "@month = :month, @year = :year")
    BigDecimal getTotalIncomeInMonthAndYear(Long userId, int month, int year);

    @Query(nativeQuery = true, value = "exec getRemainingIncomeUpToPreviousMonthByMonthAndYear @userId = :userId, " +
            "@month = :month, @year = :year")
    BigDecimal getRemainingIncomeUpToPreviousMonthByMonthAndYear(Long userId, int month, int year);

    @Query(nativeQuery = true, value = "exec getTotalExpensesUpToPreviousMonth @userId = :userId, " +
            "@month = :month, @year = :year")
    BigDecimal getTotalExpensesUpToPreviousMonth(Long userId, int month, int year);

    @Query(nativeQuery = true, value = "exec getTotalExpenseInMonthAndYear @userId = :userId, " +
            "@month = :month, @year = :year")
    BigDecimal getTotalExpenseInMonthAndYear(Long userId, int month, int year);

    @Query(nativeQuery = true, value = "exec getAvailableBalanceOfUser @userId = :userId")
    BigDecimal getAvailableBalanceOfUser(Long userId);
}
