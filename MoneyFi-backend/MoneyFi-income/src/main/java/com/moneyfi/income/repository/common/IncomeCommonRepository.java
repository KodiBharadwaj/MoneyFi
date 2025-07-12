package com.moneyfi.income.repository.common;

import com.moneyfi.income.service.dto.request.AccountStatementRequestDto;
import com.moneyfi.income.service.dto.response.*;

import java.util.List;

public interface IncomeCommonRepository {

    List<IncomeDetailsDto> getAllIncomesByDate(Long userId, int month, int year, String category, boolean deleteStatus);

    List<IncomeDeletedDto> getDeletedIncomesInAMonth(Long userId, int month, int year);

    List<IncomeDetailsDto> getAllIncomesByYear(Long userId, int year, String category, boolean deleteStatus);

    List<AccountStatementResponseDto> getAccountStatementOfUser(Long userId, AccountStatementRequestDto inputDto);

    UserDetailsForStatementDto getUserDetailsForAccountStatement(Long userId);

    OverviewPageDetailsDto getOverviewPageTileDetails(Long userId, int month, int year);
}
