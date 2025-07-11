package com.moneyfi.goal.repository.common.impl;

import com.moneyfi.goal.exceptions.QueryValidationException;
import com.moneyfi.goal.repository.common.GoalCommonRepository;
import com.moneyfi.goal.service.dto.response.GoalDetailsDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static com.moneyfi.goal.utils.StringConstants.USER_ID;

@Repository
public class GoalCommonRepositoryImpl implements GoalCommonRepository {

    @PersistenceContext
    private EntityManager entityManager;

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
