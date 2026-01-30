package com.moneyfi.transaction.service.transaction.dto.response;

import com.moneyfi.transaction.service.expense.dto.response.ExpenseDetailsDto;
import com.moneyfi.transaction.service.income.dto.response.IncomeDetailsDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GmailSyncTransactionsResponse {
    private List<IncomeDetailsDto> incomes;
    private List<ExpenseDetailsDto> expenses;
}
