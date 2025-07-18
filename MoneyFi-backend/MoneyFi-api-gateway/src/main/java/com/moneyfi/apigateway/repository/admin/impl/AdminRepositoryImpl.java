package com.moneyfi.apigateway.repository.admin.impl;

import com.moneyfi.apigateway.exceptions.QueryValidationException;
import com.moneyfi.apigateway.model.common.ContactUs;
import com.moneyfi.apigateway.repository.admin.AdminRepository;
import com.moneyfi.apigateway.service.admin.dto.AdminOverviewPageDto;
import com.moneyfi.apigateway.service.admin.dto.UserGridDto;
import com.moneyfi.apigateway.util.enums.UserStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class AdminRepositoryImpl implements AdminRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public AdminOverviewPageDto getAdminOverviewPageDetails() {
        try {
            Query query = entityManager.createNativeQuery(
                    "exec getAdminOverviewPageDetails ")
                    .unwrap(NativeQuery.class)
                    .setResultTransformer(Transformers.aliasToBean(AdminOverviewPageDto.class));

            return (AdminOverviewPageDto) query.getSingleResult();
        } catch (Exception e){
            e.printStackTrace();
            throw new QueryValidationException("Error occurred while fetching admin overview page details");
        }
    }

    @Override
    public List<ContactUs> getContactUsDetailsOfUsers() {
        try {
            Query query = entityManager.createNativeQuery(
                            "exec getContactUsDetailsOfUsers ")
                    .unwrap(NativeQuery.class);

            return query.getResultList();
        } catch (Exception e){
            e.printStackTrace();
            throw new QueryValidationException("Error occurred while fetching contact us details of users'");
        }
    }

    @Override
    public List<UserGridDto> getUserDetailsGridForAdmin(String status) {
        List<UserGridDto> userGridDetails = new ArrayList<>();

        if(status.equalsIgnoreCase(UserStatus.ACTIVE.name())){
            try {
                Query query = entityManager.createNativeQuery(
                                "exec getActiveUserDetailsForAdmin ")
                        .unwrap(NativeQuery.class)
                        .setResultTransformer(Transformers.aliasToBean(UserGridDto.class));

                userGridDetails.addAll(query.getResultList());
            } catch (Exception e){
                e.printStackTrace();
                throw new QueryValidationException("Error occurred while fetching fetching active user grid details");
            }
        } else if(status.equalsIgnoreCase(UserStatus.DELETED.name())){
            try {
                Query query = entityManager.createNativeQuery(
                                "exec getDeletedUserDetailsForAdmin ")
                        .unwrap(NativeQuery.class)
                        .setResultTransformer(Transformers.aliasToBean(UserGridDto.class));

                userGridDetails.addAll(query.getResultList());
            } catch (Exception e){
                e.printStackTrace();
                throw new QueryValidationException("Error occurred while fetching fetching deleted user grid details");
            }
        } else if(status.equalsIgnoreCase(UserStatus.BLOCKED.name())){
            try {
                Query query = entityManager.createNativeQuery(
                                "exec getBlockedUserDetailsForAdmin ")
                        .unwrap(NativeQuery.class)
                        .setResultTransformer(Transformers.aliasToBean(UserGridDto.class));

                userGridDetails.addAll(query.getResultList());
            } catch (Exception e){
                e.printStackTrace();
                throw new QueryValidationException("Error occurred while fetching fetching blocked user grid details");
            }
        } else {
            try {
                Query query = entityManager.createNativeQuery(
                                "exec getBlockedUserDetailsForAdmin ")
                        .unwrap(NativeQuery.class)
                        .setResultTransformer(Transformers.aliasToBean(UserGridDto.class));

                userGridDetails.addAll(query.getResultList());
            } catch (Exception e){
                e.printStackTrace();
                throw new QueryValidationException("Error occurred while fetching fetching blocked user grid details");
            }

            try {
                Query query = entityManager.createNativeQuery(
                                "exec getDeletedUserDetailsForAdmin ")
                        .unwrap(NativeQuery.class)
                        .setResultTransformer(Transformers.aliasToBean(UserGridDto.class));

                userGridDetails.addAll(query.getResultList());
            } catch (Exception e){
                e.printStackTrace();
                throw new QueryValidationException("Error occurred while fetching fetching deleted user grid details");
            }

            try {
                Query query = entityManager.createNativeQuery(
                                "exec getActiveUserDetailsForAdmin ")
                        .unwrap(NativeQuery.class)
                        .setResultTransformer(Transformers.aliasToBean(UserGridDto.class));

                userGridDetails.addAll(query.getResultList());
            } catch (Exception e){
                e.printStackTrace();
                throw new QueryValidationException("Error occurred while fetching fetching active user grid details");
            }
        }

        return userGridDetails;
    }
}
