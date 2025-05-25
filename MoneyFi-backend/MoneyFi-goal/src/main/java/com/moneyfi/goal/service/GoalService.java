package com.moneyfi.goal.service;

import com.moneyfi.goal.model.GoalModel;
import com.moneyfi.goal.service.dto.response.GoalDetailsDto;

import java.math.BigDecimal;
import java.util.List;

public interface GoalService {

    GoalModel save(GoalModel goal, BigDecimal amountToBeAdded, String authHeader);

    GoalModel addAmount(Long id, BigDecimal amount, String authHeader);

    List<GoalDetailsDto> getAllGoals(Long userId);

    BigDecimal getCurrentTotalGoalIncome(Long userId);

    BigDecimal getTargetTotalGoalIncome(Long userId);

    GoalModel updateByGoalName(Long id, GoalModel goal, String authHeader);

    boolean deleteGoalById(Long id, String authHeader);
}
