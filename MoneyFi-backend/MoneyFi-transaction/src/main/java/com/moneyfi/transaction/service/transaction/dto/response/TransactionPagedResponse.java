package com.moneyfi.transaction.service.transaction.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionPagedResponse<T> {
    private List<T> data;
    private Long totalCount;
    private BigDecimal totalAmount;
}
