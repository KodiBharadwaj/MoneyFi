package com.moneyfi.budget.utils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class StringConstants {

    private StringConstants() {}

    public static final String EUREKA_INCOME_SERVICE_URL = "http://MONEYFI-INCOME/api/v1/income";
    public static final String EUREKA_EXPENSE_SERVICE_URL = "http://MONEYFI-EXPENSE/api/v1/expense";

    public static String changeTransactionTimeToTwelveHourFormat(String transactionTime) {
        if (transactionTime == null) return null;

        DateTimeFormatter input = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter output = DateTimeFormatter.ofPattern("hh:mm a");
        return LocalTime.parse(transactionTime, input).format(output);
    }
}
