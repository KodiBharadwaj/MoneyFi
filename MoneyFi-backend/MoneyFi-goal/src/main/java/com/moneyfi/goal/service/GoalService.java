package com.moneyfi.goal.service;

import com.moneyfi.goal.model.GoalModel;

import java.util.List;

public interface GoalService {

    public GoalModel save(GoalModel goal);

    public GoalModel addAmount(int id, double amount);

    public List<GoalModel> getAllGoals(int userId);

    public Double getCurrentTotalGoalIncome(int userId);

    public Double getTargetTotalGoalIncome(int userId);

    public GoalModel updateByGoalName(int id, GoalModel goal);

    public boolean deleteGoalById(int id);
}
