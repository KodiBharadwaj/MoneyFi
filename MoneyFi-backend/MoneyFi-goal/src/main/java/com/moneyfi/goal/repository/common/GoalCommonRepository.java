package com.moneyfi.goal.repository.common;

import com.moneyfi.goal.model.GoalModel;
import com.moneyfi.goal.service.dto.response.GoalDetailsDto;

import java.math.BigDecimal;
import java.util.List;

public interface GoalCommonRepository {
    List<GoalDetailsDto> getAllGoalsByUserId(Long userId);

    BigDecimal getCurrentTotalGoalIncome(Long userId);

    BigDecimal getTotalTargetGoalIncome(Long userId);
}
