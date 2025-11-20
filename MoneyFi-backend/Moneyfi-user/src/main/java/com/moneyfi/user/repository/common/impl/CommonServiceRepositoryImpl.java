package com.moneyfi.user.repository.common.impl;

import com.moneyfi.user.exceptions.QueryValidationException;
import com.moneyfi.user.repository.common.CommonServiceRepository;
import com.moneyfi.user.service.common.dto.response.UserNotificationResponseDto;
import com.moneyfi.user.service.common.dto.response.UserRequestStatusDto;
import com.moneyfi.user.service.profile.dto.ProfileDetailsDto;
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
    public ProfileDetailsDto getProfileDetailsOfUser(String username) {
        try {
            Query query = entityManager.createNativeQuery(
                            "exec getProfileDetailsOfUser " +
                                    "@username = :username ")
                    .setParameter("username", username)
                    .unwrap(NativeQuery.class)
                    .setResultTransformer(Transformers.aliasToBean(ProfileDetailsDto.class));
            return (ProfileDetailsDto) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
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
    public List<String> getBirthdayAndAnniversaryUsersList(int month, int day, String occasion) {
        List<String> userNames = new ArrayList<>();
        try {
            Query query = entityManager.createNativeQuery(
                            "exec getBirthdayOrAnniversaryUserEmailAndName " +
                                    "@month = :month, " +
                                    "@day = :day, " +
                                    "@occasion = :occasion")
                    .setParameter("month", month)
                    .setParameter("day", day)
                    .setParameter("occasion", occasion);
            userNames.addAll(query.getResultList());
            return userNames;
        } catch (Exception e) {
            e.printStackTrace();
            throw new QueryValidationException("Error occurred while fetching " + occasion + " user names");
        }
    }

}
