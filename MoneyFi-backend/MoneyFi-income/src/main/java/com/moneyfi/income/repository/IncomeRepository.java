package com.moneyfi.income.repository;

import com.moneyfi.income.model.IncomeModel;
import io.swagger.v3.oas.annotations.Operation;
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

}
