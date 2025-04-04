package com.moneyfi.goal.repository;

import com.moneyfi.goal.model.GoalModel;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface GoalRepository extends JpaRepository<GoalModel, Long> {

    public List<GoalModel> findByUserId(Long userId);

    @Query("select g from GoalModel g where g.userId=:userId and g.goalName=:goalName")
    public GoalModel findByUserIdAndGoalName(Long userId, String goalName);

    @Modifying
    @Transactional
    @Query("Delete from GoalModel g where g.userId=:userId and g.goalName=:goalName")
    public void deleteParticularGoalByGoalName(Long userId, String goalName);

    @Query("select sum(g.currentAmount) from GoalModel g where g.userId = :userId")
//    @Query(nativeQuery = true, value = "exec getCurrentTotalGoalIncome @userId = :userId")
    public Double getCurrentTotalGoalIncome(Long userId);

    @Query("select sum(g.targetAmount) from GoalModel g where g.userId = :userId")
//    @Query(nativeQuery = true, value = "exec getTargetTotalGoalIncome @userId = :userId")
    public Double getTargetTotalGoalIncome(Long userId);
}
