package com.moneyfi.goal.repository;

import com.moneyfi.goal.model.GoalModel;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;


public interface GoalRepository extends JpaRepository<GoalModel, Long> {

    @Query(nativeQuery = true, value = "exec getTotalCurrentGoalIncome @userId = :userId")
    BigDecimal getCurrentTotalGoalIncome(Long userId);

    @Query(nativeQuery = true, value = "exec getTotalTargetGoalIncome @userId = :userId")
    BigDecimal getTotalTargetGoalIncome(Long userId);

    @Query(nativeQuery = true, value = "exec getAvailableBalanceOfUser @userId = :userId")
    BigDecimal getAvailableBalanceOfUser(Long userId);
}
