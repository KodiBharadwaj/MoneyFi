package com.moneyfi.transaction.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalModelDto {
    private Long id;
    private Long userId;
    private String goalName;
    private BigDecimal currentAmount;
    private BigDecimal recurringAmount;
    private BigDecimal targetAmount;
    private LocalDateTime deadLine;
    private Integer categoryId;
    private Boolean deleted;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
