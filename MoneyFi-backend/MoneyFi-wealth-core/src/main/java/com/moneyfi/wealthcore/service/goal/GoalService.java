package com.moneyfi.wealthcore.service.goal;

import com.moneyfi.wealthcore.model.GoalModel;
import com.moneyfi.wealthcore.service.goal.dto.response.GoalDetailsDto;
import com.moneyfi.wealthcore.service.goal.dto.response.GoalTileDetailsDto;
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
