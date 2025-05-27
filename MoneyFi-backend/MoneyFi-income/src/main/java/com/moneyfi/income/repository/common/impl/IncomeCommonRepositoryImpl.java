package com.moneyfi.income.repository.common.impl;

import com.moneyfi.income.service.dto.response.IncomeDeletedDto;
import com.moneyfi.income.exceptions.QueryValidationException;
import com.moneyfi.income.repository.common.IncomeCommonRepository;
import com.moneyfi.income.service.dto.response.IncomeDetailsDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Repository
public class IncomeCommonRepositoryImpl implements IncomeCommonRepository {

    @Autowired
    private EntityManager entityManager;

    @Override
    public List<IncomeDetailsDto> getAllIncomesByDate(Long userId, int month, int year, String category, boolean deleteStatus) {
        try {
            List<IncomeDetailsDto> incomesList = new ArrayList<>();

            if(category.equalsIgnoreCase("all")){
                Query query = entityManager.createNativeQuery(
                                "exec [getAllIncomesByMonthAndYear] " +
                                        "@userId = :userId, " +
                                        "@month = :month, " +
                                        "@year = :year, " +
                                        "@deleteStatus = :deleteStatus")
                        .setParameter("userId", userId)
                        .setParameter("month", month)
                        .setParameter("year", year)
                        .setParameter("deleteStatus", deleteStatus)
                        .unwrap(NativeQuery.class)
                        .setResultTransformer(Transformers.aliasToBean(IncomeDetailsDto.class));

                incomesList.addAll(query.getResultList());
                return incomesList;
            }
            else {
                Query query = entityManager.createNativeQuery(
                                "exec [getAllIncomesByMonthAndYearAndByCategory] " +
                                        "@userId = :userId, " +
                                        "@month = :month, " +
                                        "@year = :year, " +
                                        "@category = :category, " +
                                        "@deleteStatus = :deleteStatus")
                        .setParameter("userId", userId)
                        .setParameter("month", month)
                        .setParameter("year", year)
                        .setParameter("category", category)
                        .setParameter("deleteStatus", deleteStatus)
                        .unwrap(NativeQuery.class)
                        .setResultTransformer(Transformers.aliasToBean(IncomeDetailsDto.class));

                incomesList.addAll(query.getResultList());
                return incomesList;
            }

        } catch (Exception e) {
            throw new QueryValidationException("Error occurred while fetching monthly income data");
        }
    }

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
            throw new QueryValidationException("Error occurred while fetching deleted income data");
        }
    }

    @Override
    public List<IncomeDetailsDto> getAllIncomesByYear(Long userId, int year, String category, boolean deleteStatus) {
        try {
            List<IncomeDetailsDto> incomesList = new ArrayList<>();

            if(category.equalsIgnoreCase("all")){
                Query query = entityManager.createNativeQuery(
                                "exec [getAllIncomesByYear] " +
                                        "@userId = :userId, " +
                                        "@year = :year, " +
                                        "@deleteStatus = :deleteStatus")
                        .setParameter("userId", userId)
                        .setParameter("year", year)
                        .setParameter("deleteStatus", deleteStatus)
                        .unwrap(NativeQuery.class)
                        .setResultTransformer(Transformers.aliasToBean(IncomeDetailsDto.class));

                incomesList.addAll(query.getResultList());
                return incomesList;
            }
            else {
                Query query = entityManager.createNativeQuery(
                                "exec [getAllIncomesByYearAndByCategory] " +
                                        "@userId = :userId, " +
                                        "@year = :year, " +
                                        "@category = :category, " +
                                        "@deleteStatus = :deleteStatus")
                        .setParameter("userId", userId)
                        .setParameter("year", year)
                        .setParameter("category", category)
                        .setParameter("deleteStatus", deleteStatus)
                        .unwrap(NativeQuery.class)
                        .setResultTransformer(Transformers.aliasToBean(IncomeDetailsDto.class));

                incomesList.addAll(query.getResultList());
                return incomesList;
            }

        } catch (Exception e) {
            throw new QueryValidationException("Error occurred while fetching yearly income data");
        }
    }

    @Override
    public BigDecimal getTotalIncomeInMonthAndYear(Long userId, int month, int year) {
        try {
            Query query = entityManager.createNativeQuery(
                            "exec [getTotalIncomeInMonthAndYear] " +
                                    "@userId = :userId, " +
                                    "@month = :month, " +
                                    "@year = :year")
                    .setParameter("userId", userId)
                    .setParameter("month", month)
                    .setParameter("year", year);

            return (BigDecimal) query.getSingleResult();

        } catch (Exception e) {
            throw new QueryValidationException("Error occurred while fetching total income");
        }
    }

    @Override
    public BigDecimal getRemainingIncomeUpToPreviousMonthByMonthAndYear(Long userId, int month, int year) {
        try {
            Query query = entityManager.createNativeQuery(
                            "exec [getRemainingIncomeUpToPreviousMonthByMonthAndYear] " +
                                    "@userId = :userId, " +
                                    "@month = :month, " +
                                    "@year = :year")
                    .setParameter("userId", userId)
                    .setParameter("month", month)
                    .setParameter("year", year);

            return (BigDecimal) query.getSingleResult();

        } catch (Exception e) {
            throw new QueryValidationException("Error occurred while fetching remaining income");
        }
    }

    @Override
    public BigDecimal getTotalExpensesUpToPreviousMonth(Long userId, int month, int year) {
        try {
            Query query = entityManager.createNativeQuery(
                            "exec [getTotalExpensesUpToPreviousMonth] " +
                                    "@userId = :userId, " +
                                    "@month = :month, " +
                                    "@year = :year")
                    .setParameter("userId", userId)
                    .setParameter("month", month)
                    .setParameter("year", year);

            return (BigDecimal) query.getSingleResult();

        } catch (Exception e) {
            throw new QueryValidationException("Error occurred while fetching total expenses upto previous month");
        }
    }

    @Override
    public BigDecimal getTotalExpenseInMonthAndYear(Long userId, int month, int year) {
        try {
            Query query = entityManager.createNativeQuery(
                            "exec [getTotalExpenseInMonthAndYear] " +
                                    "@userId = :userId, " +
                                    "@month = :month, " +
                                    "@year = :year")
                    .setParameter("userId", userId)
                    .setParameter("month", month)
                    .setParameter("year", year);

            return (BigDecimal) query.getSingleResult();

        } catch (Exception e) {
            throw new QueryValidationException("Error occurred while fetching total expense in a month");
        }
    }

    @Override
    public BigDecimal getAvailableBalanceOfUser(Long userId) {
        try {
            Query query = entityManager.createNativeQuery(
                            "exec [getAvailableBalanceOfUser] " +
                                    "@userId = :userId")
                    .setParameter("userId", userId);

            return (BigDecimal) query.getSingleResult();

        } catch (Exception e) {
            throw new QueryValidationException("Error occurred while fetching total remaining balance of a user");
        }
    }

}
