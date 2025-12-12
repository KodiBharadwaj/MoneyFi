package com.moneyfi.transaction.service.income.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OverviewPageDetailsDto {
    private BigDecimal availableBalance;
    private BigDecimal totalExpense;
    private BigDecimal totalBudget;
    private BigDecimal budgetProgress;
    private BigDecimal totalGoalIncome;
    private BigDecimal goalProgress;
}
