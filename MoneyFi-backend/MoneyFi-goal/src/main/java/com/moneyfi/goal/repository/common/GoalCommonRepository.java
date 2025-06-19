package com.moneyfi.goal.repository.common;

import com.moneyfi.goal.service.dto.response.GoalDetailsDto;

import java.util.List;

public interface GoalCommonRepository {
    List<GoalDetailsDto> getAllGoalsByUserId(Long userId);
}
