package com.moneyfi.budget.repository.common.impl;

import com.moneyfi.budget.exceptions.QueryValidationException;
import com.moneyfi.budget.repository.common.BudgetCommonRepository;
import com.moneyfi.budget.service.dto.response.BudgetDetailsDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class BudgetCommonRepositoryImpl implements BudgetCommonRepository {

    @Autowired
    private EntityManager entityManager;

    @Override
    public List<BudgetDetailsDto> getBudgetsByUserId(Long userId, String category) {
        try {
            List<BudgetDetailsDto> budgetList = new ArrayList<>();

            if(category.equalsIgnoreCase("all")){
                Query query = entityManager.createNativeQuery(
                                "exec [getAllBudgetsByUserId] " +
                                        "@userId = :userId")
                        .setParameter("userId", userId)
                        .unwrap(NativeQuery.class)
                        .setResultTransformer(Transformers.aliasToBean(BudgetDetailsDto.class));

                budgetList.addAll(query.getResultList());
                return budgetList;
            }
            else {
                Query query = entityManager.createNativeQuery(
                                "exec [getAllBudgetsByUserIdAndByCategory] " +
                                        "@userId = :userId, " +
                                        "@category = :category")
                        .setParameter("userId", userId)
                        .setParameter("category", category)
                        .unwrap(NativeQuery.class)
                        .setResultTransformer(Transformers.aliasToBean(BudgetDetailsDto.class));

                budgetList.addAll(query.getResultList());
                return budgetList;
            }

        } catch (Exception e) {
            throw new QueryValidationException("Error occurred while fetching budget data");
        }
    }
}
