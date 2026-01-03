package com.moneyfi.transaction.service.transaction.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParsedTransaction {
    private Integer categoryId;
    private Long gmailProcessedId;
    private String description;
    private BigDecimal amount;
    private String transactionType;
    private LocalDateTime transactionDate;
}
