package com.moneyfi.apigateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParsedTransaction {

    private BigDecimal amount;
    private String description;
    private String type;   // CREDIT / DEBIT
    private LocalDateTime transactionDate;
}

