package com.moneyfi.goal.repository.common.impl;

import com.moneyfi.goal.exceptions.QueryValidationException;
import com.moneyfi.goal.repository.common.GoalCommonRepository;
import com.moneyfi.goal.service.dto.response.GoalDetailsDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Repository
public class GoalCommonRepositoryImpl implements GoalCommonRepository {

    @Autowired
    private EntityManager entityManager;

    @Override
    public List<GoalDetailsDto> getAllGoalsByUserId(Long userId) {
        try {
            List<GoalDetailsDto> goalList = new ArrayList<>();

            Query query = entityManager.createNativeQuery(
                            "exec [getAllGoalsByUserId] " +
                                    "@userId = :userId")
                    .setParameter("userId", userId)
                    .unwrap(NativeQuery.class)
                    .setResultTransformer(Transformers.aliasToBean(GoalDetailsDto.class));

            goalList.addAll(query.getResultList());
            return goalList;

        } catch (Exception e) {
            throw new QueryValidationException("Error occurred while fetching goal data");
        }
    }

    @Override
    public BigDecimal getCurrentTotalGoalIncome(Long userId) {
        try {
            Query query = entityManager.createNativeQuery(
                            "exec [getTotalCurrentGoalIncome] " +
                                    "@userId = :userId")
                    .setParameter("userId", userId);

            return (BigDecimal) query.getSingleResult();

        } catch (Exception e) {
            throw new QueryValidationException("Error occurred while fetching total goal income");
        }
    }

    @Override
    public BigDecimal getTotalTargetGoalIncome(Long userId) {
        try {
            Query query = entityManager.createNativeQuery(
                            "exec [getTotalTargetGoalIncome] " +
                                    "@userId = :userId")
                    .setParameter("userId", userId);

            return (BigDecimal) query.getSingleResult();

        } catch (Exception e) {
            throw new QueryValidationException("Error occurred while fetching total target goal income");
        }
    }
}
