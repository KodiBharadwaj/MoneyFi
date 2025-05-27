package com.moneyfi.income.repository;

import com.moneyfi.income.model.IncomeDeleted;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncomeDeletedRepository extends JpaRepository<IncomeDeleted, Long> {

    IncomeDeleted findByIncomeId(Long incomeId);

    void deleteByIncomeId(Long incomeId);
}
