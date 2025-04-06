package com.moneyfi.income.repository;

import com.moneyfi.income.model.IncomeModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface IncomeRepository extends JpaRepository<IncomeModel, Long> {

    @Query("select i from IncomeModel i where i.userId = :userId")
    List<IncomeModel> findIncomesOfUser(Long userId);

    @Query("SELECT i FROM IncomeModel i WHERE i.userId = :userId " +
            "AND EXTRACT(MONTH FROM i.date) = :month " +
            "AND EXTRACT(YEAR FROM i.date) = :year " +
            "AND i.is_deleted = :deleteStatus")
    List<IncomeModel> getAllIncomesByDate(Long userId, int month, int year, boolean deleteStatus);

    @Query("SELECT MONTH(i.date) AS month, SUM(i.amount) AS total " +
            "FROM IncomeModel i " +
            "where i.userId = :userId AND YEAR(i.date)=:year AND i.is_deleted = :deleteStatus " +
            "GROUP BY MONTH(i.date) " +
            "ORDER BY month ASC")
    List<Object[]> findMonthlyIncomes(Long userId, int year, boolean deleteStatus);

    @Query("SELECT i FROM IncomeModel i WHERE i.userId = :userId " +
            "AND EXTRACT(YEAR FROM i.date) = :year " +
            "AND i.is_deleted = :deleteStatus")
    List<IncomeModel> getAllIncomesByYear(Long userId, int year, boolean deleteStatus);


    @Query("select sum(i.amount) from IncomeModel i where i.userId = :userId " +
            "and EXTRACT(YEAR FROM i.date) = :year and EXTRACT(MONTH FROM i.date) = :month " +
            "and i.is_deleted = false")
    BigDecimal getTotalIncomeInMonthAndYear(Long userId, int month, int year);


    @Query("select sum(i.amount) from IncomeModel i where i.userId = :userId " +
            "and EXTRACT(YEAR FROM i.date) = :year and EXTRACT(MONTH FROM i.date) < :month " +
            "and i.is_deleted = false")
    BigDecimal getRemainingIncomeUpToPreviousMonthByMonthAndYear(Long userId, int month, int year);

    @Query("select i from IncomeModel i where i.userId = :userId " +
            "and i.source = :source and i.category = :category")
    IncomeModel getIncomeBySourceAndCategory(Long userId, String source, String category);

    @Query("select i.amount from IncomeModel i where i.id = :incomeId")
    BigDecimal getIncomeByIncomeId(Long incomeId);
}
