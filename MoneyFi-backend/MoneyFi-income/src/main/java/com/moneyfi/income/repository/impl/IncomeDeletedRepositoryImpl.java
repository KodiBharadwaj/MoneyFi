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
                        (BigDecimal) row[0],
                        (String) row[1],
                        ((Date) row[2]).toLocalDate(),
                        (String) row[3],
                        (Boolean) row[4],
                        ((Integer) row[5])
                ))
                .toList();
    }

}
