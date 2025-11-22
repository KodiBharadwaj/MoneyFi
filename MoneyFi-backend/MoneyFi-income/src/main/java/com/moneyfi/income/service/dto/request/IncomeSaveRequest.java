package com.moneyfi.income.service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IncomeSaveRequest {
    private BigDecimal amount;
    private String source;
    private String date;
    private String category;
    private Boolean recurring;
}
