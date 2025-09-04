package com.moneyfi.apigateway.repository.common.impl;

import com.moneyfi.apigateway.exceptions.QueryValidationException;
import com.moneyfi.apigateway.repository.common.CommonServiceRepository;
import com.moneyfi.apigateway.service.common.dto.response.ProfileDetailsDto;
import com.moneyfi.apigateway.service.common.dto.response.UserNotificationResponseDto;
import com.moneyfi.apigateway.service.common.dto.response.UserRequestStatusDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public UserRequestStatusDto trackUserRequestUsingReferenceNumber(String referenceNumber) {
        try {
            Query query = entityManager.createNativeQuery(
                            "exec getStausOfUserRequestUsingReferenceNumber " +
                                    "@referenceNumber = :referenceNumber ")
                    .setParameter("referenceNumber", referenceNumber)
                    .unwrap(NativeQuery.class)
                    .setResultTransformer(Transformers.aliasToBean(UserRequestStatusDto.class));

            return (UserRequestStatusDto) query.getSingleResult();

        } catch (NoResultException e) {
            // Graceful fallback
            return null;

        } catch (Exception e) {
            e.printStackTrace();
            throw new QueryValidationException("Error occurred while fetching user request status");
        }
    }

    @Override
    public List<UserNotificationResponseDto> getUserNotifications(String username) {
        List<UserNotificationResponseDto> userNotificationsList = new ArrayList<>();
        try {
            Query query = entityManager.createNativeQuery(
                            "exec getUserScheduledNotifications " +
                                    "@username = :username ")
                    .setParameter("username", username)
                    .unwrap(NativeQuery.class)
                    .setResultTransformer(Transformers.aliasToBean(UserNotificationResponseDto.class));

            userNotificationsList.addAll(query.getResultList());
            return userNotificationsList;
        } catch (Exception e) {
            e.printStackTrace();
            throw new QueryValidationException("Error occurred while fetching user notifications");
        }
    }

    @Override
    public List<String> getBirthdayUserNames(int month, int day) {
        List<String> userNames = new ArrayList<>();
        try {
            Query query = entityManager.createNativeQuery(
                            "exec getBirthdayUserEmailAndName " +
                                    "@month = :month, " +
                                    "@day = :day ")
                    .setParameter("month", month)
                    .setParameter("day", day);
            userNames.addAll(query.getResultList());
            return userNames;
        } catch (Exception e) {
            e.printStackTrace();
            throw new QueryValidationException("Error occurred while fetching birthday user names");
        }
    }

}
