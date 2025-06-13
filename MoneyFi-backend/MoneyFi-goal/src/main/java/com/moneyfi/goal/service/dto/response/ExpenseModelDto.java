package com.moneyfi.goal.service.dto.response;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseModelDto {
    private Long id;
    private Long userId;
    private String category;
    private BigDecimal amount;
    private LocalDate date;
    private boolean recurring;
    private String description;
}
