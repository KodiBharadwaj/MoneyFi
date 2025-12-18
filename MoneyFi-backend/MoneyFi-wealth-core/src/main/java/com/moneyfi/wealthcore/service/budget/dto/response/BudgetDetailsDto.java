package com.moneyfi.wealthcore.service.budget.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

import static com.moneyfi.wealthcore.utils.StringConstants.DATE_TIME_PATTERN;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetDetailsDto {
    private Long id;
    private String category;
    private BigDecimal currentSpending;
    private BigDecimal moneyLimit;
    private Long progressPercentage;
    @JsonFormat(pattern = DATE_TIME_PATTERN, timezone = "Asia/Kolkata")
    private Timestamp createdAt;
    @JsonFormat(pattern = DATE_TIME_PATTERN, timezone = "Asia/Kolkata")
    private Timestamp updatedAt;
}
