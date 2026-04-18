package com.moneyfi.transaction.service.income.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import static com.moneyfi.transaction.utils.constants.StringConstants.DATE_MONTH_YEAR_FORMAT;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionsListRequestDto {
    @NotBlank
    private String category;
    private boolean deleteStatus;
    @NotNull
    @JsonFormat(pattern = DATE_MONTH_YEAR_FORMAT)
    private LocalDate date;
    @NotNull
    private Long startIndex;
    @NotNull
    private Long threshold;
    private String sortBy;
    private String sortOrder;
    @NotBlank
    private String requestType;
}
