package com.moneyfi.transaction.service.income.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncomeUpdateRequest {
    private Long id;
    private BigDecimal amount;
    private String source;
    private String date;
    private Integer categoryId;
    private Boolean recurring;
    private String description;
}
