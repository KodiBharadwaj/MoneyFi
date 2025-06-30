package com.moneyfi.apigateway.repository.admin.impl;

import com.moneyfi.apigateway.exceptions.QueryValidationException;
import com.moneyfi.apigateway.model.common.ContactUs;
import com.moneyfi.apigateway.repository.admin.AdminRepository;
import com.moneyfi.apigateway.service.admin.dto.AdminOverviewPageDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Repository;

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
            throw new QueryValidationException("Error occurred while fetching admin overview page details");
        }
    }
}
