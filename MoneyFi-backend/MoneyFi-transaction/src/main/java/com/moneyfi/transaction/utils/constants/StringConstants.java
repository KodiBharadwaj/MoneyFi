package com.moneyfi.transaction.utils.constants;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class StringConstants {

    private StringConstants() {}

    public static final String USER_ID = "userId";
    public static final String MONTH = "month";
    public static final String YEAR = "year";
    public static final String DELETE_STATUS = "deleteStatus";
    public static final String DATE = "date";
    public static final String AMOUNT = "amount";
    public static final String TYPE = "type";
    public static final String DESC = "desc";
    public static final String SOURCE = "source";
    public static final String REQUEST_TYPE = "requestType";
    public static final String CATEGORY = "category";
    public static final String OFFSET = "offset";
    public static final String LIMIT = "limit";
    public static final String ALL = "ALL";

    public static final String MONTHLY = "MONTHLY";
    public static final String YEARLY = "YEARLY";

    public static final String CATEGORY_ID = "categoryId";
    public static final String DESCRIPTION = "description";
    public static final String TRANSACTION_DATE = "transactionDate";
    public static final String SOMETHING_WENT_WRONG = "Something went wrong";

    public static final String INCOME_ALREADY_PRESENT_MESSAGE = "Income with this source and category is already there";


    public static final String INCOME_NOT_FOUND = "Income Details not found";
    public static final String CATEGORY_ID_INVALID = "Please send valid category";
    public static final String CATEGORY_NOT_ALIGN_MESSAGE = "Category not aligns with transaction type";
    public static final String USER_ID_EMPTY = "User id is empty";
    public static final String INVALID_INPUT = "Invalid input";
    public static final String ERROR_GENERATION_EXCEL = "Error in generating excel report";
    public static final String YES = "Yes";
    public static final String NO = "No";

    public static final String DATE_MONTH_YEAR_FORMAT = "yyyy-MM-dd";

    public static String changeTransactionTimeToTwelveHourFormat(String transactionTime) {
        if (transactionTime == null) return null;

        DateTimeFormatter input = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter output = DateTimeFormatter.ofPattern("hh:mm a");
        return LocalTime.parse(transactionTime, input).format(output);
    }
}
