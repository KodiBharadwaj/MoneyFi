package com.moneyfi.wealthcore.service.goal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoalTileDetailsDto {
    private Map<String, BigDecimal> goalTileDetails;
}
