package com.moneyfi.income.repository.common.impl;

import com.moneyfi.income.dto.IncomeDeletedDto;
import com.moneyfi.income.exceptions.QueryValidationException;
import com.moneyfi.income.repository.common.IncomeCommonRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Repository
public class IncomeCommonRepositoryImpl implements IncomeCommonRepository {

    @Autowired
    private EntityManager entityManager;

    @Override
    public List<IncomeDeletedDto> getDeletedIncomesInAMonth(Long userId, int month, int year) {

        try {
            List<IncomeDeletedDto> incomeListDeleted = new ArrayList<>();

            Query query = entityManager.createNativeQuery(
                    "exec [getDeletedIncomesInAMonth] " +
                            "@userId = :userId, " +
                            "@month = :month, " +
                            "@year = :year")
                    .setParameter("userId", userId)
                    .setParameter("month", month)
                    .setParameter("year", year)
                    .unwrap(NativeQuery.class)
                    .setResultListTransformer(Transformers.aliasToBean(IncomeDeletedDto.class));

            incomeListDeleted.addAll(query.getResultList());
            return incomeListDeleted;
        } catch (Exception e) {
            throw new QueryValidationException("Error occurred while fetching data");
        }
    }

}
