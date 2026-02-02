package com.moneyfi.transaction.utils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class StringConstants {

    private StringConstants() {}

    public static final String USER_ID = "userId";
    public static final String MONTH = "month";
    public static final String YEAR = "year";
    public static final String DELETE_STATUS = "deleteStatus";
    public static final String START_DATE = "startDate";
    public static final String END_DATE = "endDate";
    public static final String CATEGORY = "category";
    public static final String CATEGORY_ID = "categoryId";
    public static final String OFFSET = "offset";
    public static final String LIMIT = "limit";

    public static final String INCOME_NOT_FOUND = "Income Details not found";
    public static final String CATEGORY_ID_INVALID = "Please send valid category";
    public static final String CATEGORY_NOT_ALIGN_MESSAGE = "Category not aligns with transaction type";
    public static final String USER_ID_EMPTY = "User id is empty";
    public static final String INVALID_INPUT = "Invalid input";
    public static final String NO_CHANGES_TO_UPDATE = "No changes to update";
    public static final String ERROR_GENERATION_EXCEL = "Error in generating excel report";
    public static final String YES = "Yes";
    public static final String NO = "No";

    public static final LocalDateTime CURRENT_DATE_TIME = LocalDateTime.now();

    public static final String ACCOUNT_STATEMENT_USER_SERVICE_URL = "http://MONEYFI-USER/api/v1/user-service/user/account-statement/email";

    public static String changeTransactionTimeToTwelveHourFormat(String transactionTime) {
        if (transactionTime == null) return null;

        DateTimeFormatter input = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter output = DateTimeFormatter.ofPattern("hh:mm a");
        return LocalTime.parse(transactionTime, input).format(output);
    }
}
