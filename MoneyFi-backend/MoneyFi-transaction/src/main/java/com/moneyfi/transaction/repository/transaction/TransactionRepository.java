package com.moneyfi.transaction.repository.transaction;

import com.moneyfi.transaction.service.expense.response.ExpenseDetailsDto;
import com.moneyfi.transaction.service.income.dto.request.AccountStatementRequestDto;
import com.moneyfi.transaction.service.income.dto.response.*;

import java.util.List;

public interface TransactionRepository {

    List<IncomeDetailsDto> getAllIncomesByDate(Long userId, int month, int year, String category, boolean deleteStatus);

    List<IncomeDeletedDto> getDeletedIncomesInAMonth(Long userId, int month, int year);

    List<IncomeDetailsDto> getAllIncomesByYear(Long userId, int year, String category, boolean deleteStatus);

    List<AccountStatementResponseDto> getAccountStatementOfUser(Long userId, AccountStatementRequestDto inputDto);

    UserDetailsForStatementDto getUserDetailsForAccountStatement(Long userId);

    OverviewPageDetailsDto getOverviewPageTileDetails(Long userId, int month, int year);

    List<ExpenseDetailsDto> getAllExpensesByDate(Long userId, int month, int year, String category, boolean deleteStatus);

    List<ExpenseDetailsDto> getAllExpensesByYear(Long userId, int year, String category, boolean deleteStatus);
}
