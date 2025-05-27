package com.moneyfi.income.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncomeDetailsDto {
    private Long id;
    private BigDecimal amount;
    private String source;
    private Date date;
    private String category;
    private boolean recurring;
}
