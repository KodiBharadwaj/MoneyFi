package com.moneyfi.transaction.service.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionCategoryResponse {
    private Map<String, BigDecimal> incomeCategory;
    private Map<String, BigDecimal> expenseCategory;
}
