package com.moneyfi.apigateway.util.constants;

import com.moneyfi.apigateway.util.enums.UserRoles;

import java.util.Map;
import java.util.Random;

public class StringUtils {

    private StringUtils() {}

    public static final String ADMIN_EMAIL = "moneyfi.owner@gmail.com";
    public static final String MESSAGE = "message";
    public static final Map<Integer, String> userRoleAssociation = Map.of(1, UserRoles.ADMIN.name(), 2, UserRoles.USER.name());

    public static final String DATE_TIME_PATTERN = "MM/dd/yyyy HH:mm:ss";
    public static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";


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
}
