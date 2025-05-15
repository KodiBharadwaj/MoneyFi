package com.moneyfi.income.repository.common;

import com.moneyfi.income.dto.IncomeDeletedDto;

import java.util.List;

public interface IncomeCommonRepository {

    List<IncomeDeletedDto> getDeletedIncomesInAMonth(Long userId, int month, int year);
}
