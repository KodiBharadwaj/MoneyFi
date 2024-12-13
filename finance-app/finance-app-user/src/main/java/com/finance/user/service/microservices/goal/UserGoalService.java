package com.finance.user.service.microservices.goal;

import com.finance.user.dto.GoalModel;

import java.util.List;

public interface UserGoalService {
    public GoalModel addGoal(int userId, GoalModel goal);

    public List<GoalModel> getAllGoals(int userId);

    public GoalModel updateGoal(int id, GoalModel goal);

    public boolean deleteGoalById(int id);
}
