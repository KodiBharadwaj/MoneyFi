package com.moneyfi.budget.service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddBudgetDto {
    private String category;
    private Integer percentage;
    private BigDecimal moneyLimit;
}
