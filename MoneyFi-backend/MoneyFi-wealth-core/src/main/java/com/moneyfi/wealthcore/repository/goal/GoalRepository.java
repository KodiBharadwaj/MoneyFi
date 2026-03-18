package com.moneyfi.wealthcore.repository.goal;

import com.moneyfi.wealthcore.model.goal.GoalModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface GoalRepository extends JpaRepository<GoalModel, Long> {

    /** SP Call */
    @Query(nativeQuery = true, value = "exec getTotalCurrentGoalIncome @userId = :userId")
    BigDecimal getCurrentTotalGoalIncome(Long userId);

    /** SP Call */
    @Query(nativeQuery = true, value = "exec getTotalTargetGoalIncome @userId = :userId")
    BigDecimal getTotalTargetGoalIncome(Long userId);

    /** SP Call */
    @Query(nativeQuery = true, value = "exec getAvailableBalanceOfUser @userId = :userId")
    BigDecimal getAvailableBalanceOfUser(Long userId);
}
