package com.finance.goal.service;

import com.finance.goal.model.GoalModel;

import java.util.List;

public interface GoalService {

    public GoalModel save(GoalModel goal);

    public List<GoalModel> getAllGoals(int userId);

    public GoalModel updateByGoalName(int id, GoalModel goal);

    public void deleteGoalById(int id);
}
