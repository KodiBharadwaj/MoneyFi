package com.moneyfi.transaction.service.income;

import com.moneyfi.transaction.service.income.dto.request.IncomeSaveRequest;
import com.moneyfi.transaction.service.income.dto.request.IncomeUpdateRequest;
import com.moneyfi.transaction.service.income.dto.request.TransactionsListRequestDto;
import com.moneyfi.transaction.service.income.dto.response.IncomeDeletedDto;
import com.moneyfi.transaction.model.income.IncomeModel;
import com.moneyfi.transaction.service.income.dto.response.IncomeDetailsDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface IncomeService {

    void saveIncome(IncomeSaveRequest incomeSaveRequest, Long userId);

    List<IncomeModel> getAllIncomes(Long userId);

    List<IncomeDetailsDto> getAllIncomesByDate(Long userId, TransactionsListRequestDto requestDto);

    byte[] getIncomesReportExcel(Long userId, TransactionsListRequestDto requestDto);

    List<IncomeDeletedDto> getDeletedIncomesInAMonth(Long userId, int month, int year);

    List<BigDecimal> getMonthlyIncomes(Long userId, int year);

    BigDecimal getTotalIncomeInMonthAndYear(Long userId, int month, int year);

    boolean incomeUpdateCheckFunction(IncomeModel incomeModel, Long userId);

    boolean incomeDeleteCheckFunction(IncomeModel incomeModel);

    boolean incomeRevertFunction(Long incomeId, Long userId);

    void updateBySource(Long id, Long userId, IncomeUpdateRequest incomeUpdateRequest);

    boolean deleteIncomeById(Long id, Long userId);

    BigDecimal getAvailableBalanceOfUser(Long userId);

    List<Object[]> getTotalIncomeInSpecifiedRange(Long userId, LocalDateTime fromDate, LocalDateTime toDate);
}
