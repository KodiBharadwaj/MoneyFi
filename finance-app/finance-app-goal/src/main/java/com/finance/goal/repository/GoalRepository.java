package com.finance.goal.repository;

import com.finance.goal.model.GoalModel;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface GoalRepository extends JpaRepository<GoalModel, Integer> {

    public List<GoalModel> findByUserId(int userId);

    @Query("select g from GoalModel g where g.userId=:userId and g.goalName=:goalName")
    public GoalModel findByUserIdAndGoalName(int userId, String goalName);

    @Modifying
    @Transactional
    @Query("Delete from GoalModel g where g.userId=:userId and g.goalName=:goalName")
    public void deleteParticularGoalByGoalName(int userId, String goalName);
}
