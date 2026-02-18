package com.moneyfi.wealthcore.repository.common.impl;

import com.moneyfi.wealthcore.exceptions.QueryValidationException;
import com.moneyfi.wealthcore.repository.common.WealthCoreRepository;
import com.moneyfi.wealthcore.service.budget.dto.response.BudgetDetailsDto;
import com.moneyfi.wealthcore.service.budget.dto.response.UserDetailsForSpendingAnalysisDto;
import com.moneyfi.wealthcore.service.goal.dto.response.GoalDetailsDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static com.moneyfi.wealthcore.utils.constants.StringConstants.USER_ID;

@Repository
public class WealthCoreRepositoryImpl implements WealthCoreRepository {
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
                Integer categoryId = Integer.parseInt(category);
                Query query = entityManager.createNativeQuery(
                                "exec [getAllBudgetsByUserIdAndByCategory] " +
                                        "@userId = :userId, " +
                                        "@month = :month, " +
                                        "@year = :year, " +
                                        "@categoryId = :categoryId")
                        .setParameter("userId", userId)
                        .setParameter("month", month)
                        .setParameter("year", year)
                        .setParameter("categoryId", categoryId)
                        .unwrap(NativeQuery.class)
                        .setResultTransformer(Transformers.aliasToBean(BudgetDetailsDto.class));
                budgetList.addAll(query.getResultList());
                return budgetList;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new QueryValidationException(e.getMessage());
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

    @Override
    public List<GoalDetailsDto> getAllGoalsByUserId(Long userId) {
        try {
            List<GoalDetailsDto> goalList = new ArrayList<>();

            Query query = entityManager.createNativeQuery(
                            "exec [getAllGoalsByUserId] " +
                                    "@userId = :userId")
                    .setParameter(USER_ID, userId)
                    .unwrap(NativeQuery.class)
                    .setResultTransformer(Transformers.aliasToBean(GoalDetailsDto.class));

            goalList.addAll(query.getResultList());
            return goalList;

        } catch (Exception e) {
            e.printStackTrace();
            throw new QueryValidationException("Error occurred while fetching goal data");
        }
    }
}
