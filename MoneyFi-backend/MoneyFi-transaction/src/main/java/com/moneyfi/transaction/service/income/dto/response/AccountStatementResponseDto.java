package com.moneyfi.transaction.service.income.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountStatementResponseDto {
    private int id;
    private Date transactionDate;
    private String transactionTime;
    private String description;
    private BigDecimal amount;
    private BigDecimal totalExpenses;
    private String creditOrDebit;
}
