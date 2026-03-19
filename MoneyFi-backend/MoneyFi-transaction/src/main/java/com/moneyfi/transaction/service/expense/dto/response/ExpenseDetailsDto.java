package com.moneyfi.transaction.service.expense.dto.response;

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
public class ExpenseDetailsDto {
    private Long id;
    private String category;
    private BigDecimal amount;
    private Date date;
    private boolean recurring;
    private String description;
    private boolean isDeleted;
    private String activeStatus;
    @JsonIgnore
    private Long totalCount;
    @JsonIgnore
    private BigDecimal totalAmount;
}
