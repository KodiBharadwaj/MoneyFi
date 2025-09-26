package com.moneyfi.budget.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpendingAnalysisResponseDto {
    private Map<String, BigDecimal> incomeByCategory;
    private Map<String, BigDecimal> expenseByCategory;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal amountAvailableTillNow;
}
