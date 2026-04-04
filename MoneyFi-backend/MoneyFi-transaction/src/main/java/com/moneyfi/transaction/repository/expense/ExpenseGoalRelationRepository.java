package com.moneyfi.transaction.repository.expense;

import com.moneyfi.transaction.model.expense.ExpenseGoalRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseGoalRelationRepository extends JpaRepository<ExpenseGoalRelation, Long> {
}
