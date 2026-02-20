package com.moneyfi.transaction.repository.transaction;

import com.moneyfi.transaction.service.expense.dto.response.ExpenseDetailsDto;
import com.moneyfi.transaction.service.income.dto.request.AccountStatementRequestDto;
import com.moneyfi.transaction.service.income.dto.request.TransactionsListRequestDto;
import com.moneyfi.transaction.service.income.dto.response.*;

import java.util.List;

public interface TransactionRepository {

    List<IncomeDetailsDto> getAllIncomesByDate(Long userId, TransactionsListRequestDto requestDto);

    List<IncomeDeletedDto> getDeletedIncomesInAMonth(Long userId, int month, int year);

    List<AccountStatementResponseDto> getAccountStatementOfUser(Long userId, AccountStatementRequestDto inputDto);

    UserDetailsForStatementDto getUserDetailsForAccountStatement(Long userId);

    OverviewPageDetailsDto getOverviewPageTileDetails(Long userId, int month, int year);

    List<ExpenseDetailsDto> getAllExpensesByDate(Long userId, TransactionsListRequestDto requestDto);

    List<Integer> getCategoryIdsBasedOnTransactionType(String categoryType);

    void updateGmailProcessedAsVerified(List<Long> gmailProsessedIdList);
}
