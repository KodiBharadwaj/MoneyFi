package com.finance.budget.repository;

import com.finance.budget.model.BudgetModel;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BudgetRepository extends JpaRepository<BudgetModel, Integer> {

    public BudgetModel findByUserId(int userId);

    @Query("select b from BudgetModel b where b.userId = :userId")
    public List<BudgetModel> getBudgetsByUserId(int userId);

    public BudgetModel findByCategory(String category);

    @Query("select b from BudgetModel b where b.userId=:userId and b.category=:category")
    public BudgetModel findByUserIdAndCatergory(int userId, String category);

    @Modifying
    @Transactional
    @Query("Delete from BudgetModel b where b.userId=:userId and b.category=:category")
    public void deleteParticularBudgetByCategory(int userId, String category);
}
