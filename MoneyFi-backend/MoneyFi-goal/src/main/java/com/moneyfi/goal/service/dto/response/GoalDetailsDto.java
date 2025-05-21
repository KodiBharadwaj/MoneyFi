package com.moneyfi.goal.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoalDetailsDto {
    private Long id;
    private String goalName;
    private BigDecimal currentAmount;
    private BigDecimal targetAmount;
    private Date deadLine;
    private String category;
}
