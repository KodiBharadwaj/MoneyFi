package com.moneyfi.wealthcore.utils;

import com.moneyfi.wealthcore.service.budget.dto.response.UserDetailsForSpendingAnalysisDto;

public class StringConstants {

    private StringConstants() {}

    public static final String BUDGET_NOT_FOUND = "Budget details not found";
    public static final String EMAIL_SENT_SUCCESS_MESSAGE = "Email sent successfully";
    public static final String EMAIL_SENT_FAILURE_MESSAGE = "Failed to send email";
    public static final String DATE_TIME_PATTERN = "MM/dd/yyyy HH:mm:ss";

    public static final String USER_ID = "userId";
    public static final String TOTAL_GOAL_AMOUNT = "totalGoalAmount";
    public static final String TOTAL_GOAL_TARGET_AMOUNT = "totalGoalTargetAmount";
    public static final String TOTAL_AVAILABLE_INCOME = "availableIncome";

    public static final String EUREKA_TRANSACTION_SERVICE_URL = "http://MONEYFI-TRANSACTION/api/v1/transaction";
    public static final String USER_SERVICE_URL_CONTROLLER = "http://MONEYFI-USER/api/v1/user-service/user";

    public static String makeUsernamePrivate(String username){
        int index = username.indexOf('@');
        return username.substring(0, index/3) + "x".repeat(index - index/3) + username.substring(index);
    }

    public static String generateDocumentPasswordForUser(UserDetailsForSpendingAnalysisDto userDetails){
        return userDetails.getName().substring(0,4).toUpperCase() + userDetails.getUsername().substring(0,4).toLowerCase();
    }
}
