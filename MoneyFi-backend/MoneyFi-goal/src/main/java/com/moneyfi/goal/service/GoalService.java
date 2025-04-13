package com.moneyfi.goal.service;

import com.moneyfi.goal.model.GoalModel;

import java.math.BigDecimal;
import java.util.List;

public interface GoalService {

    GoalModel save(GoalModel goal);

    GoalModel addAmount(Long id, BigDecimal amount);

    List<GoalModel> getAllGoals(Long userId);

    BigDecimal getCurrentTotalGoalIncome(Long userId);

    BigDecimal getTargetTotalGoalIncome(Long userId);

    GoalModel updateByGoalName(Long id, GoalModel goal);

    boolean deleteGoalById(Long id);
}
