package com.moneyfi.goal.service;

import com.moneyfi.goal.model.GoalModel;
import com.moneyfi.goal.service.dto.response.GoalDetailsDto;

import java.math.BigDecimal;
import java.util.List;

public interface GoalService {

    GoalModel save(GoalModel goal);

    GoalModel addAmount(Long id, BigDecimal amount);

    List<GoalDetailsDto> getAllGoals(Long userId);

    BigDecimal getCurrentTotalGoalIncome(Long userId);

    BigDecimal getTargetTotalGoalIncome(Long userId);

    GoalModel updateByGoalName(Long id, Long userId, GoalModel goal);

    boolean deleteGoalById(Long id);
}
