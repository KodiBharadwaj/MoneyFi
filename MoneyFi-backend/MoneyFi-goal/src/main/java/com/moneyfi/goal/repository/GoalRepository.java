package com.moneyfi.goal.repository;

import com.moneyfi.goal.model.GoalModel;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;


public interface GoalRepository extends JpaRepository<GoalModel, Long> {

    List<GoalModel> findByUserId(Long userId);

    @Query("select g from GoalModel g where g.userId=:userId and g.goalName=:goalName")
    GoalModel findByUserIdAndGoalName(Long userId, String goalName);

    @Modifying
    @Transactional
    @Query("Delete from GoalModel g where g.userId=:userId and g.goalName=:goalName")
    void deleteParticularGoalByGoalName(Long userId, String goalName);

    @Query(nativeQuery = true, value = "exec getCurrentTotalGoalIncome @userId = :userId")
    BigDecimal getCurrentTotalGoalIncome(Long userId);

    @Query(nativeQuery = true, value = "exec getTargetTotalGoalIncome @userId = :userId")
    BigDecimal getTargetTotalGoalIncome(Long userId);
}
