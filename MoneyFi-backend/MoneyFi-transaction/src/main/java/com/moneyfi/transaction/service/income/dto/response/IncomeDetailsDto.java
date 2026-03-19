package com.moneyfi.transaction.service.income.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncomeDetailsDto {
    private Long id;
    private BigDecimal amount;
    private String source;
    private Date date;
    private String category;
    private boolean recurring;
    private String description;
    private String activeStatus;
    @JsonIgnore
    private Long totalCount;
    @JsonIgnore
    private BigDecimal totalAmount;
}
