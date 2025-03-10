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

//    @Query("SELECT i FROM IncomeModel i WHERE i.userId = :userId " +
//            "AND EXTRACT(MONTH FROM i.date) = :month " +
//            "AND EXTRACT(YEAR FROM i.date) = :year " +
//            "AND i.is_deleted = :deleteStatus")
    @Query(nativeQuery = true, value = "exec getAllIncomesByMonthAndYear @userId = :userId, " +
            "@month = :month, @year = :year, @deleteStatus = :deleteStatus")
    public  List<IncomeModel> getAllIncomesByDate(int userId, int month, int year, boolean deleteStatus);

//    @Query("SELECT MONTH(i.date) AS month, SUM(i.amount) AS total " +
//            "FROM IncomeModel i " +
//            "where i.userId = :userId AND YEAR(i.date)=:year AND i.is_deleted = :deleteStatus " +
//            "GROUP BY MONTH(i.date) " +
//            "ORDER BY month ASC")
    @Query(nativeQuery = true, value = "exec findMonthlyIncomesListInAYear @userId = :userId, " +
        "@year = :year, @deleteStatus = :deleteStatus")
    public List<Object[]> findMonthlyIncomes(int userId, int year, boolean deleteStatus);

//    @Query("SELECT i FROM IncomeModel i WHERE i.userId = :userId " +
//            "AND EXTRACT(YEAR FROM i.date) = :year " +
//            "AND i.is_deleted = :deleteStatus")
    @Query(nativeQuery = true, value = "exec getAllIncomesByYear @userId = :userId, @year = :year, " +
        "@deleteStatus = :deleteStatus")
    public List<IncomeModel> getAllIncomesByYear(int userId, int year, boolean deleteStatus);

    @Query(nativeQuery = true, value = "exec getTotalIncomeInMonthAndYear @userId = :userId, " +
        "@month = :month, @year = :year")
    public Double getTotalIncomeInMonthAndYear(int userId, int month, int year);


}
