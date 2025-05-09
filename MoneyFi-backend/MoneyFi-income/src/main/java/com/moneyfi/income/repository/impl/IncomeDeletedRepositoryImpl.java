package com.moneyfi.income.repository.impl;

import com.moneyfi.income.dto.IncomeDeletedDto;
import com.moneyfi.income.repository.IncomeDeletedRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

public class IncomeDeletedRepositoryImpl implements IncomeDeletedRepositoryCustom {

    @Autowired
    private EntityManager entityManager;

    public List<IncomeDeletedDto> getDeletedIncomesInAMonth(Long userId, int month, int year) {
        Query query = entityManager.createNativeQuery("exec getDeletedIncomesInAMonth :userId, :month, :year");
        query.setParameter("userId", userId);
        query.setParameter("month", month);
        query.setParameter("year", year);

        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(row -> new IncomeDeletedDto(
                        (Long) row[0],
                        (BigDecimal) row[1],
                        (String) row[2],
                        ((Date) row[3]).toLocalDate(),
                        (String) row[4],
                        (Boolean) row[5],
                        ((Integer) row[6])
                ))
                .toList();
    }

}
