package com.moneyfi.apigateway.repository.common;

import java.util.List;

public interface CommonServiceRepository {
    List<String> getBirthdayAndAnniversaryUsersList(int month, int day, String occasion);

    List<String> getCategoriesBasedOnTransactionType(String name);
}
