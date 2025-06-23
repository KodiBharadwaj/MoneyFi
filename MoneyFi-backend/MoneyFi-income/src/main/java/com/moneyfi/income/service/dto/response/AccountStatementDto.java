package com.moneyfi.income.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountStatementDto {
    private Date transactionDate;
    private String description;
    private BigDecimal amount;
    private BigDecimal totalExpenses;
    private String creditOrDebit;
}
