package com.moneyfi.transaction.repository.transaction.impl;

import com.moneyfi.transaction.exceptions.QueryValidationException;
import com.moneyfi.transaction.repository.transaction.TransactionRepository;
import com.moneyfi.transaction.service.expense.response.ExpenseDetailsDto;
import com.moneyfi.transaction.service.income.dto.request.AccountStatementRequestDto;
import com.moneyfi.transaction.service.income.dto.response.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import static com.moneyfi.transaction.utils.StringConstants.*;
import static com.moneyfi.transaction.utils.StringConstants.YEAR;

@Repository
public class TransactionRepositoryImpl implements TransactionRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private static final String NO_MONTHLY_INCOME_DATE = "Error occurred while fetching monthly income data";
    private static final String NO_YEARLY_INCOME_DATE = "Error occurred while fetching yearly income data";
    private static final String NO_DELETED_INCOME_DATE = "Error occurred while fetching deleted income data";

    @Override
    public List<IncomeDetailsDto> getAllIncomesByDate(Long userId, int month, int year, String category, boolean deleteStatus) {
        try {
            List<IncomeDetailsDto> incomesList = new ArrayList<>();
            if (category.equalsIgnoreCase("all")) {
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
            } else {
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
            throw new QueryValidationException(NO_MONTHLY_INCOME_DATE);
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
            throw new QueryValidationException(NO_DELETED_INCOME_DATE);
        }
    }

    @Override
    public List<IncomeDetailsDto> getAllIncomesByYear(Long userId, int year, String category, boolean deleteStatus) {
        try {
            List<IncomeDetailsDto> incomesList = new ArrayList<>();
            if (category.equalsIgnoreCase("all")) {
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
            } else {
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
            throw new QueryValidationException(NO_YEARLY_INCOME_DATE);
        }
    }

    @Override
    public List<AccountStatementResponseDto> getAccountStatementOfUser(Long userId, AccountStatementRequestDto inputDto) {
        Date startDate = Date.valueOf(inputDto.getFromDate());
        Date endDate = Date.valueOf(inputDto.getToDate());

        List<AccountStatementResponseDto> accountStatement = new ArrayList<>();
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
                    .setResultTransformer(Transformers.aliasToBean(AccountStatementResponseDto.class));
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
                            "exec getUserDetailsForPdfGeneration " +
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

    @Override
    public List<ExpenseDetailsDto> getAllExpensesByDate(Long userId, int month, int year, String category, boolean deleteStatus) {
        try {
            List<ExpenseDetailsDto> expenseList = new ArrayList<>();

            if(category.equalsIgnoreCase("all")){
                Query query = entityManager.createNativeQuery(
                                "exec [getAllExpensesByMonthAndYear] " +
                                        "@userId = :userId, " +
                                        "@month = :month, " +
                                        "@year = :year, " +
                                        "@deleteStatus = :deleteStatus")
                        .setParameter(USER_ID, userId)
                        .setParameter(MONTH, month)
                        .setParameter(YEAR, year)
                        .setParameter(DELETE_STATUS, deleteStatus)
                        .unwrap(NativeQuery.class)
                        .setResultTransformer(Transformers.aliasToBean(ExpenseDetailsDto.class));

                expenseList.addAll(query.getResultList());
                return expenseList;
            }
            else {
                Query query = entityManager.createNativeQuery(
                                "exec [getAllExpensesByMonthAndYearAndByCategory] " +
                                        "@userId = :userId, " +
                                        "@month = :month, " +
                                        "@year = :year, " +
                                        "@category = :category, " +
                                        "@deleteStatus = :deleteStatus")
                        .setParameter(USER_ID, userId)
                        .setParameter(MONTH, month)
                        .setParameter(YEAR, year)
                        .setParameter("category", category)
                        .setParameter(DELETE_STATUS, deleteStatus)
                        .unwrap(NativeQuery.class)
                        .setResultTransformer(Transformers.aliasToBean(ExpenseDetailsDto.class));

                expenseList.addAll(query.getResultList());
                return expenseList;
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new QueryValidationException("Error occurred while fetching monthly expense data");
        }
    }

    @Override
    public List<ExpenseDetailsDto> getAllExpensesByYear(Long userId, int year, String category, boolean deleteStatus) {
        try {
            List<ExpenseDetailsDto> expenseList = new ArrayList<>();

            if(category.equalsIgnoreCase("all")){
                Query query = entityManager.createNativeQuery(
                                "exec [getAllExpensesByYear] " +
                                        "@userId = :userId, " +
                                        "@year = :year, " +
                                        "@deleteStatus = :deleteStatus")
                        .setParameter(USER_ID, userId)
                        .setParameter(YEAR, year)
                        .setParameter(DELETE_STATUS, deleteStatus)
                        .unwrap(NativeQuery.class)
                        .setResultTransformer(Transformers.aliasToBean(ExpenseDetailsDto.class));

                expenseList.addAll(query.getResultList());
                return expenseList;
            }
            else {
                Query query = entityManager.createNativeQuery(
                                "exec [getAllExpensesByYearAndByCategory] " +
                                        "@userId = :userId, " +
                                        "@year = :year, " +
                                        "@category = :category, " +
                                        "@deleteStatus = :deleteStatus")
                        .setParameter(USER_ID, userId)
                        .setParameter(YEAR, year)
                        .setParameter("category", category)
                        .setParameter(DELETE_STATUS, deleteStatus)
                        .unwrap(NativeQuery.class)
                        .setResultTransformer(Transformers.aliasToBean(ExpenseDetailsDto.class));

                expenseList.addAll(query.getResultList());
                return expenseList;
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new QueryValidationException("Error occurred while fetching yearly expense data");
        }
    }
}
