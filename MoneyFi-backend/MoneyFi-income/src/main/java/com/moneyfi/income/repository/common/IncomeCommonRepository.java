package com.moneyfi.income.repository.common;

import com.moneyfi.income.service.dto.response.AccountStatementDto;
import com.moneyfi.income.service.dto.response.IncomeDeletedDto;
import com.moneyfi.income.service.dto.response.IncomeDetailsDto;
import com.moneyfi.income.service.dto.response.UserDetailsForStatementDto;

import java.time.LocalDate;
import java.util.List;

public interface IncomeCommonRepository {

    List<IncomeDetailsDto> getAllIncomesByDate(Long userId, int month, int year, String category, boolean deleteStatus);

    List<IncomeDeletedDto> getDeletedIncomesInAMonth(Long userId, int month, int year);

    List<IncomeDetailsDto> getAllIncomesByYear(Long userId, int year, String category, boolean deleteStatus);

    List<AccountStatementDto> getAccountStatementOfUser(Long userId, LocalDate fromDate, LocalDate toDate);

    UserDetailsForStatementDto getUserDetailsForAccountStatement(Long userId);
}
