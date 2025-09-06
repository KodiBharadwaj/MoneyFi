package com.moneyfi.budget.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetDetailsDto {
    private Long id;
    private String category;
    private BigDecimal currentSpending;
    private BigDecimal moneyLimit;
    private Long progressPercentage;
}
