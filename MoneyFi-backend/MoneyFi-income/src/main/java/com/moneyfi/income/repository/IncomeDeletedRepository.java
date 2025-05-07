package com.moneyfi.income.repository;

import com.moneyfi.income.dto.IncomeDeletedDto;
import com.moneyfi.income.model.IncomeDeleted;
import com.moneyfi.income.repository.impl.IncomeDeletedRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IncomeDeletedRepository extends JpaRepository<IncomeDeleted, Long>, IncomeDeletedRepositoryCustom {

    List<IncomeDeletedDto> getDeletedIncomesInAMonth(Long userId, int month, int year);
}
