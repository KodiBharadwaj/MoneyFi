package com.moneyfi.transaction.repository.income;

import com.moneyfi.transaction.model.income.IncomeDeleted;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncomeDeletedRepository extends JpaRepository<IncomeDeleted, Long> {

    IncomeDeleted findByIncomeId(Long incomeId);

    void deleteByIncomeId(Long incomeId);
}
