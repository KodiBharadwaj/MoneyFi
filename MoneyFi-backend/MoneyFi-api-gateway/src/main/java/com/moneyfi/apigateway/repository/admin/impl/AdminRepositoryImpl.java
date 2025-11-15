package com.moneyfi.apigateway.repository.admin.impl;

import com.moneyfi.apigateway.exceptions.QueryValidationException;
import com.moneyfi.apigateway.repository.admin.AdminRepository;
import com.moneyfi.apigateway.service.admin.dto.response.*;
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
    public List<AdminSchedulesResponseDto> getAllActiveSchedulesOfAdmin() {
        List<AdminSchedulesResponseDto> scheduleList = new ArrayList<>();
        try {
            Query query = entityManager.createNativeQuery(
                            "exec getAllActiveSchedulesOfAdmin ")
                    .unwrap(NativeQuery.class)
                    .setResultTransformer(Transformers.aliasToBean(AdminSchedulesResponseDto.class));

            scheduleList.addAll(query.getResultList());
            return scheduleList;
        } catch (Exception e){
            e.printStackTrace();
            throw new QueryValidationException("Error occurred while fetching schedules for admin");
        }
    }
}
