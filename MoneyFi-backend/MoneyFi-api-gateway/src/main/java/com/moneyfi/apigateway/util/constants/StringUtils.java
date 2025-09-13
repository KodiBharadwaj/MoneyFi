package com.moneyfi.apigateway.util.constants;

import com.moneyfi.apigateway.util.enums.ReasonEnum;
import com.moneyfi.apigateway.util.enums.UserRoles;

import java.util.Map;
import java.util.Random;

import static com.moneyfi.apigateway.util.enums.ReasonEnum.*;

public class StringUtils {

    private StringUtils() {}

    public static final String ADMIN_EMAIL = "moneyfi.owner@gmail.com";
    public static final String MESSAGE = "message";

    public static final String ERROR = "error";
    public static final String USERNAME_PASSWORD_REQUIRED = "Username and password are required";
    public static final String USER_NOT_FOUND = "User not found. Please sign up";
    public static final String ACCOUNT_BLOCKED = "Account Blocked! Please contact admin";
    public static final String ACCOUNT_DELETED = "Account Deleted! Please contact admin";
    public static final String INCORRECT_PASSWORD = "Incorrect password. Please try again";
    public static final String INVALID_CREDENTIALS = "Invalid Credentials Entered";
    public static final String LOGIN_ERROR = "An error occurred during login";

    public static final String CLOUDINARY_CLOUD_NAME = "cloud_name";
    public static final String CLOUDINARY_API_KEY = "api_key";
    public static final String CLOUDINARY_API_SECRET = "api_secret";
    public static final String CLOUDINARY_SECURE = "secure";

    public static final Map<Integer, String> userRoleAssociation = Map.of(1, UserRoles.ADMIN.name(), 2, UserRoles.USER.name(), 3, UserRoles.DEVELOPER.name());
    public static final Map<String, Integer> templateIdAssociation = Map.of("profile-template", 1);
    public static final Map<ReasonEnum, Integer> reasonCodeIdAssociation = Map.of(BLOCK_ACCOUNT, 1, PASSWORD_CHANGE, 2, NAME_CHANGE, 3,
            UNBLOCK_ACCOUNT, 4, DELETE_ACCOUNT, 5, ACCOUNT_RETRIEVAL, 6, PHONE_NUMBER_CHANGE, 7);

    public static final String DATE_TIME_PATTERN = "MM/dd/yyyy HH:mm:ss";
    public static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public static final String DAILY_QUOTE_EXTERNAL_API_URL = "https://zenquotes.io/api/random";

    public static String generateVerificationCode() {
        Random random = new Random();
        int verificationCode = 100000 + random.nextInt(900000);
        return String.valueOf(verificationCode);
    }

    public static String generateAlphabetCode() {
        StringBuilder code = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 5; i++) {
            int index = random.nextInt(ALPHABET.length());
            code.append(ALPHABET.charAt(index));
        }

        return code.toString();
    }

    public static String generateFileNameForUserProfilePicture(Long userId, String username){
        return "profile_pic_" + (userId) + "_" +
                username.substring(0,username.indexOf('@'));
    }
}
