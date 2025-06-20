package com.moneyfi.expense.repository.common.impl;

import com.moneyfi.expense.exceptions.QueryValidationException;
import com.moneyfi.expense.repository.common.ExpenseCommonRepository;
import com.moneyfi.expense.service.dto.response.ExpenseDetailsDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static com.moneyfi.expense.utils.StringConstants.*;

@Repository
public class ExpenseCommonRepositoryImpl implements ExpenseCommonRepository {

    private final EntityManager entityManager;

    public ExpenseCommonRepositoryImpl(EntityManager entityManager){
        this.entityManager = entityManager;
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
            throw new QueryValidationException("Error occurred while fetching yearly expense data");
        }
    }
}
