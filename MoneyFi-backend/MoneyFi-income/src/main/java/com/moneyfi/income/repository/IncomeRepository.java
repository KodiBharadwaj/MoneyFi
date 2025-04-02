package com.moneyfi.income.repository;

import com.moneyfi.income.model.IncomeModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IncomeRepository extends JpaRepository<IncomeModel, Integer> {

    @Query("select i from IncomeModel i where i.userId = :userId")
    public List<IncomeModel> findIncomesOfUser(int userId);

    @Query("SELECT i FROM IncomeModel i WHERE i.userId = :userId " +
            "AND EXTRACT(MONTH FROM i.date) = :month " +
            "AND EXTRACT(YEAR FROM i.date) = :year " +
            "AND i.is_deleted = :deleteStatus")
    public  List<IncomeModel> getAllIncomesByDate(int userId, int month, int year, boolean deleteStatus);

    @Query("SELECT MONTH(i.date) AS month, SUM(i.amount) AS total " +
            "FROM IncomeModel i " +
            "where i.userId = :userId AND YEAR(i.date)=:year AND i.is_deleted = :deleteStatus " +
            "GROUP BY MONTH(i.date) " +
            "ORDER BY month ASC")
    public List<Object[]> findMonthlyIncomes(int userId, int year, boolean deleteStatus);

    @Query("SELECT i FROM IncomeModel i WHERE i.userId = :userId " +
            "AND EXTRACT(YEAR FROM i.date) = :year " +
            "AND i.is_deleted = :deleteStatus")
    public List<IncomeModel> getAllIncomesByYear(int userId, int year, boolean deleteStatus);


    @Query("select sum(i.amount) from IncomeModel i where i.userId = :userId " +
            "and EXTRACT(YEAR FROM i.date) = :year and EXTRACT(MONTH FROM i.date) = :month " +
            "and i.is_deleted = false")
    public Double getTotalIncomeInMonthAndYear(int userId, int month, int year);


    @Query("select sum(i.amount) from IncomeModel i where i.userId = :userId " +
            "and EXTRACT(YEAR FROM i.date) = :year and EXTRACT(MONTH FROM i.date) < :month " +
            "and i.is_deleted = false")
    public Double getRemainingIncomeUpToPreviousMonthByMonthAndYear(int userId, int month, int year);

    @Query("select i from IncomeModel i where i.userId = :userId " +
            "and i.source = :source and i.category = :category")
    public IncomeModel getIncomeBySourceAndCategory(int userId, String source, String category);

    @Query("select i.amount from IncomeModel i where i.id = :incomeId")
    public Double getIncomeByIncomeId(int incomeId);
}
