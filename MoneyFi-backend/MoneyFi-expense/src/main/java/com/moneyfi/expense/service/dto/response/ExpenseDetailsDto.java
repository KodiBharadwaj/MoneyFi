package com.moneyfi.expense.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseDetailsDto {
    private Long id;
    private String category;
    private BigDecimal amount;
    private Date date;
    private boolean recurring;
    private String description;
    private boolean isDeleted;
}
