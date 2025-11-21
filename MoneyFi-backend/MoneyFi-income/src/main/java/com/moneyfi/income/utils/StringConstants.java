package com.moneyfi.income.utils;

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
    public static final String OFFSET = "offset";
    public static final String LIMIT = "limit";

    public static final String INCOME_NOT_FOUND = "Income Details not found";
    public static final String NO_CHANGES_TO_UPDATE = "No changes to update";

    public static final String JWT_SERVICE_API_GATEWAY_URL = "http://localhost:8765/api/v1/userProfile/getUserId/";
    public static final String ACCOUNT_STATEMENT_USER_SERVICE_URL = "http://MONEYFI-USER/api/v1/user-service/account-statement/email";

    public static String changeTransactionTimeToTwelveHourFormat(String transactionTime) {
        if (transactionTime == null) return null;

        DateTimeFormatter input = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter output = DateTimeFormatter.ofPattern("hh:mm a");
        return LocalTime.parse(transactionTime, input).format(output);
    }
}
