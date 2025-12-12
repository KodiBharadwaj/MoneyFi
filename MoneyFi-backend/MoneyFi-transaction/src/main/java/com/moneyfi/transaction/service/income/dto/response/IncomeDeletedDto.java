package com.moneyfi.transaction.service.income.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncomeDeletedDto {
    private Long id;
    private BigDecimal amount;
    private String source;
    private LocalDate date;
    private String category;
    private boolean recurring;
    private Integer daysRemained;
}
