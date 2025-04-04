package com.moneyfi.budget.repository;

import com.moneyfi.budget.model.BudgetModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BudgetRepository extends JpaRepository<BudgetModel, Long> {

    @Query("select b from BudgetModel b where b.userId = :userId")
//    @Query(nativeQuery = true, value = "exec getAllBudgetsByUserId @userId = :userId")
    public List<BudgetModel> getBudgetsByUserId(Long userId);

}
