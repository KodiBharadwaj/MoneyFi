package com.moneyfi.wealthcore.service.budget.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddBudgetDto {
    @NotNull
    private Integer categoryId;
    @NotNull
    private Integer percentage;
    @NotNull
    private BigDecimal moneyLimit;
}
