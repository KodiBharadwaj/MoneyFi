package com.moneyfi.transaction.repository.income;

import com.moneyfi.transaction.model.income.IncomeDeleted;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IncomeDeletedRepository extends JpaRepository<IncomeDeleted, Long> {

    /** Spring JPA */
    IncomeDeleted findByIncomeId(Long incomeId);

    /** Spring JPA */
    void deleteByIncomeId(Long incomeId);
}
