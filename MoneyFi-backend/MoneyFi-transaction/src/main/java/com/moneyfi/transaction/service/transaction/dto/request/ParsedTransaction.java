package com.moneyfi.transaction.service.transaction.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParsedTransaction {
    @NotNull
    private Integer categoryId;
    @NotNull
    private Long gmailProcessedId;
    @NotBlank
    private String description;
    @NotNull
    private BigDecimal amount;
    @NotBlank
    private String transactionType;
    @NotNull
    private LocalDateTime transactionDate;
}
