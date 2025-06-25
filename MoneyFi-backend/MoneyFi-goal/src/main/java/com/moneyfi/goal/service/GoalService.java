package com.moneyfi.goal.service;

import com.moneyfi.goal.model.GoalModel;
import com.moneyfi.goal.service.dto.response.GoalDetailsDto;
import com.moneyfi.goal.service.dto.response.GoalTileDetailsDto;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

public interface GoalService {

    GoalDetailsDto save(GoalModel goal, BigDecimal amountToBeAdded, String authHeader);

    GoalDetailsDto addAmount(Long id, BigDecimal amount, String authHeader);

    List<GoalDetailsDto> getAllGoals(Long userId);

    BigDecimal getCurrentTotalGoalIncome(Long userId);

    BigDecimal getTargetTotalGoalIncome(Long userId);

    ResponseEntity<GoalDetailsDto> updateByGoalName(Long id, GoalModel goal, String authHeader);

    boolean deleteGoalById(Long id, String authHeader);

    GoalTileDetailsDto getGoalTileDetails(Long userId);
}
