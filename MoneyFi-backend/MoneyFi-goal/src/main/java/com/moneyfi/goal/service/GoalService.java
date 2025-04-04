package com.moneyfi.goal.service;

import com.moneyfi.goal.model.GoalModel;

import java.util.List;

public interface GoalService {

    public GoalModel save(GoalModel goal);

    public GoalModel addAmount(Long id, double amount);

    public List<GoalModel> getAllGoals(Long userId);

    public Double getCurrentTotalGoalIncome(Long userId);

    public Double getTargetTotalGoalIncome(Long userId);

    public GoalModel updateByGoalName(Long id, GoalModel goal);

    public boolean deleteGoalById(Long id);
}
