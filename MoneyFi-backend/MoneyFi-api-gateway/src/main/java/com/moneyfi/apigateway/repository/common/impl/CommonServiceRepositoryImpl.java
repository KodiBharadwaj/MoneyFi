package com.moneyfi.apigateway.repository.common.impl;

import com.moneyfi.apigateway.exceptions.QueryValidationException;
import com.moneyfi.apigateway.repository.common.CommonServiceRepository;
import com.moneyfi.apigateway.service.common.dto.ProfileDetailsDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Repository;

@Repository
public class CommonServiceRepositoryImpl implements CommonServiceRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public ProfileDetailsDto getProfileDetailsOfUser(Long userId) {
        try {
            Query query = entityManager.createNativeQuery(
                    "exec getProfileDetailsOfUser " +
                            "@userId = :userId ")
                    .setParameter("userId", userId)
                    .unwrap(NativeQuery.class)
                    .setResultTransformer(Transformers.aliasToBean(ProfileDetailsDto.class));

            return (ProfileDetailsDto) query.getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            throw new QueryValidationException("Error occurred while fetching user profile data");
        }
    }
}
