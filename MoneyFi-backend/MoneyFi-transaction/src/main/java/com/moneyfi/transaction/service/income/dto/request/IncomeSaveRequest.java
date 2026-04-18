package com.moneyfi.transaction.service.income.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IncomeSaveRequest {
    @NotNull
    private BigDecimal amount;
    @NotBlank
    private String source;
    @NotBlank
    private String date;
    @NotNull
    private Integer categoryId;
    @NotNull
    private Boolean recurring;
    private String description;
}
