package com.moneyfi.transaction.service.income.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import static com.moneyfi.transaction.utils.StringConstants.DATE_MONTH_YEAR_FORMAT;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionsListRequestDto {
    private String category;
    private boolean deleteStatus;
    @JsonFormat(pattern = DATE_MONTH_YEAR_FORMAT)
    private LocalDate date;
    private Long startIndex;
    private Long threshold;
    private String sortBy;
    private String sortOrder;
    private String requestType;
}
