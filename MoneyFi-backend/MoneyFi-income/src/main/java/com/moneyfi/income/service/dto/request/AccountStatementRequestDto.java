package com.moneyfi.income.service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountStatementRequestDto {
    private LocalDate fromDate;
    private LocalDate toDate;
    private int startIndex;
    private int threshold;
}
