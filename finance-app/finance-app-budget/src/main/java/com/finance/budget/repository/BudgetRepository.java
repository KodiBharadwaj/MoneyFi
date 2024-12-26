package com.finance.budget.repository;

import com.finance.budget.model.BudgetModel;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BudgetRepository extends JpaRepository<BudgetModel, Integer> {

    @Query("select b from BudgetModel b where b.userId = :userId")
    public List<BudgetModel> getBudgetsByUserId(int userId);

}
