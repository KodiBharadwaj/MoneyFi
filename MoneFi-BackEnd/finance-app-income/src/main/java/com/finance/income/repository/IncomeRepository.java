package com.finance.income.repository;

import com.finance.income.model.IncomeModel;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IncomeRepository extends JpaRepository<IncomeModel, Integer> {

    @Query("select i from IncomeModel i where i.userId = :userId")
    public List<IncomeModel> findIncomesOfUser(int userId);

    @Query("select i from IncomeModel i where i.userId=:userId and i.source=:source")
    public IncomeModel findByUserIdAndSource(int userId, String source);

    @Query("SELECT i FROM IncomeModel i WHERE i.userId = :userId " +
            "AND EXTRACT(MONTH FROM i.date) = :month " +
            "AND EXTRACT(YEAR FROM i.date) = :year")
    public  List<IncomeModel> getAllIncomesByDate(int userId, int month, int year);

    @Query("SELECT MONTH(i.date) AS month, SUM(i.amount) AS total " +
            "FROM IncomeModel i " +
            "where i.userId = :userId AND YEAR(i.date)=:year " +
            "GROUP BY MONTH(i.date) " +
            "ORDER BY month ASC")
    public List<Object[]> findMonthlyIncomes(int userId, int year);

    @Query("SELECT i FROM IncomeModel i WHERE i.userId = :userId " +
            "AND EXTRACT(YEAR FROM i.date) = :year")
    public List<IncomeModel> getAllIncomesByYear(int userId, int year);


}
