package com.moneyfi.apigateway.repository.common.impl;

import com.moneyfi.apigateway.exceptions.QueryValidationException;
import com.moneyfi.apigateway.repository.common.CommonServiceRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class CommonServiceRepositoryImpl implements CommonServiceRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<String> getBirthdayAndAnniversaryUsersList(int month, int day, String occasion) {
        List<String> userNames = new ArrayList<>();
        try {
            Query query = entityManager.createNativeQuery(
                            "exec getBirthdayOrAnniversaryUserEmailAndName " +
                                    "@month = :month, " +
                                    "@day = :day, " +
                                    "@occasion = :occasion")
                    .setParameter("month", month)
                    .setParameter("day", day)
                    .setParameter("occasion", occasion);
            userNames.addAll(query.getResultList());
            return userNames;
        } catch (Exception e) {
            e.printStackTrace();
            throw new QueryValidationException("Error occurred while fetching " + occasion + " user names");
        }
    }

    @Override
    public List<String> getCategoriesBasedOnTransactionType(String categoryType) {
        try {
            List<String> incomeCategoryIdList = new ArrayList<>();
            Query query = entityManager.createNativeQuery(
                            "exec [getCategoriesByCategoryType] " +
                                    "@categoryType = :categoryType ")
                    .setParameter("categoryType", categoryType);
            incomeCategoryIdList.addAll(query.getResultList());
            return incomeCategoryIdList;
        } catch (Exception e) {
            e.printStackTrace();
            throw new QueryValidationException("Income Category Ids not found");
        }
    }
}
