package com.moneyfi.income.repository.common.impl;

import com.moneyfi.income.service.dto.request.AccountStatementInputDto;
import com.moneyfi.income.service.dto.response.*;
import com.moneyfi.income.exceptions.QueryValidationException;
import com.moneyfi.income.repository.common.IncomeCommonRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import static com.moneyfi.income.utils.StringConstants.*;

@Repository
public class IncomeCommonRepositoryImpl implements IncomeCommonRepository {

    private final EntityManager entityManager;

    public IncomeCommonRepositoryImpl(EntityManager entityManager){
        this.entityManager = entityManager;
    }

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
                        .setParameter(USER_ID, userId)
                        .setParameter(MONTH, month)
                        .setParameter(YEAR, year)
                        .setParameter(DELETE_STATUS, deleteStatus)
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
                        .setParameter(USER_ID, userId)
                        .setParameter(MONTH, month)
                        .setParameter(YEAR, year)
                        .setParameter(CATEGORY, category)
                        .setParameter(DELETE_STATUS, deleteStatus)
                        .unwrap(NativeQuery.class)
                        .setResultTransformer(Transformers.aliasToBean(IncomeDetailsDto.class));

                incomesList.addAll(query.getResultList());
                return incomesList;
            }

        } catch (Exception e) {
            e.printStackTrace();
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
                    .setParameter(USER_ID, userId)
                    .setParameter(MONTH, month)
                    .setParameter(YEAR, year)
                    .unwrap(NativeQuery.class)
                    .setResultListTransformer(Transformers.aliasToBean(IncomeDeletedDto.class));

            incomeListDeleted.addAll(query.getResultList());
            return incomeListDeleted;
        } catch (Exception e) {
            e.printStackTrace();
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
                        .setParameter(USER_ID, userId)
                        .setParameter(YEAR, year)
                        .setParameter(DELETE_STATUS, deleteStatus)
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
                        .setParameter(USER_ID, userId)
                        .setParameter(YEAR, year)
                        .setParameter(CATEGORY, category)
                        .setParameter(DELETE_STATUS, deleteStatus)
                        .unwrap(NativeQuery.class)
                        .setResultTransformer(Transformers.aliasToBean(IncomeDetailsDto.class));

                incomesList.addAll(query.getResultList());
                return incomesList;
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new QueryValidationException("Error occurred while fetching yearly income data");
        }
    }

    @Override
    public List<AccountStatementDto> getAccountStatementOfUser(Long userId, AccountStatementInputDto inputDto) {

        Date startDate = Date.valueOf(inputDto.getFromDate());
        Date endDate = Date.valueOf(inputDto.getToDate());
        List<AccountStatementDto> accountStatement = new ArrayList<>();

        try {
            Query query = entityManager.createNativeQuery(
                    "exec getAccountStatementOfUser " +
                            "@userId = :userId, " +
                            "@startDate = :startDate, " +
                            "@endDate = :endDate, " +
                            "@offset = :offset, " +
                            "@limit = :limit ")
                    .setParameter(USER_ID, userId)
                    .setParameter(START_DATE, startDate)
                    .setParameter(END_DATE, endDate)
                    .setParameter(OFFSET, inputDto.getStartIndex())
                    .setParameter(LIMIT, inputDto.getThreshold())
                    .unwrap(NativeQuery.class)
                    .setResultTransformer(Transformers.aliasToBean(AccountStatementDto.class));

            accountStatement.addAll(query.getResultList());
            return accountStatement;

        } catch (Exception e) {
            e.printStackTrace();
            throw new QueryValidationException("Error occurred while fetching user's account statement");
        }
    }

    @Override
    public UserDetailsForStatementDto getUserDetailsForAccountStatement(Long userId) {
        try {
            Query query = entityManager.createNativeQuery(
                    "exec getUserDetailsForAccountStatement " +
                            "@userId = :userId ")
                    .setParameter(USER_ID, userId)
                    .unwrap(NativeQuery.class)
                    .setResultTransformer(Transformers.aliasToBean(UserDetailsForStatementDto.class));

            return (UserDetailsForStatementDto) query.getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            throw new QueryValidationException("Error occurred while fetching user's details");
        }
    }

    @Override
    public OverviewPageDetailsDto getOverviewPageTileDetails(Long userId, int month, int year) {
        try {
            Query query = entityManager.createNativeQuery(
                            "exec getOverviewPageDetails " +
                                    "@userId = :userId, " +
                                    "@month = :month, " +
                                    "@year = :year ")
                    .setParameter(USER_ID, userId)
                    .setParameter(MONTH, month)
                    .setParameter(YEAR, year)
                    .unwrap(NativeQuery.class)
                    .setResultTransformer(Transformers.aliasToBean(OverviewPageDetailsDto.class));

            return (OverviewPageDetailsDto) query.getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            throw new QueryValidationException("Error occurred while fetching user's overview page details");
        }
    }
}
