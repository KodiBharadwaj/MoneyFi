package com.moneyfi.budget.repository.common.impl;

import com.moneyfi.budget.exceptions.QueryValidationException;
import com.moneyfi.budget.repository.common.BudgetCommonRepository;
import com.moneyfi.budget.service.dto.response.BudgetDetailsDto;
import com.moneyfi.budget.service.dto.response.UserDetailsForSpendingAnalysisDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class BudgetCommonRepositoryImpl implements BudgetCommonRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<BudgetDetailsDto> getBudgetsByUserId(Long userId, int month, int year, String category) {
        try {
            List<BudgetDetailsDto> budgetList = new ArrayList<>();

            if(category.equalsIgnoreCase("all")){
                Query query = entityManager.createNativeQuery(
                                "exec [getAllBudgetsByUserId] " +
                                        "@userId = :userId, " +
                                        "@month = :month, " +
                                        "@year = :year")
                        .setParameter("userId", userId)
                        .setParameter("month", month)
                        .setParameter("year", year)
                        .unwrap(NativeQuery.class)
                        .setResultTransformer(Transformers.aliasToBean(BudgetDetailsDto.class));

                budgetList.addAll(query.getResultList());
                return budgetList;
            }
            else {
                Query query = entityManager.createNativeQuery(
                                "exec [getAllBudgetsByUserIdAndByCategory] " +
                                        "@userId = :userId, " +
                                        "@month = :month, " +
                                        "@year = :year, " +
                                        "@category = :category")
                        .setParameter("userId", userId)
                        .setParameter("month", month)
                        .setParameter("year", year)
                        .setParameter("category", category)
                        .unwrap(NativeQuery.class)
                        .setResultTransformer(Transformers.aliasToBean(BudgetDetailsDto.class));

                budgetList.addAll(query.getResultList());
                return budgetList;
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new QueryValidationException("Error occurred while fetching budget data");
        }
    }

    @Override
    public UserDetailsForSpendingAnalysisDto getUserDetailsForAccountSpendingAnalysisStatement(Long userId) {
        try {
            Query query = entityManager.createNativeQuery(
                            "exec getUserDetailsForPdfGeneration " +
                                    "@userId = :userId ")
                    .setParameter("userId", userId)
                    .unwrap(NativeQuery.class)
                    .setResultTransformer(Transformers.aliasToBean(UserDetailsForSpendingAnalysisDto.class));

            return (UserDetailsForSpendingAnalysisDto) query.getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            throw new QueryValidationException("Error occurred while fetching user's details");
        }
    }
}
