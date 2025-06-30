package com.moneyfi.apigateway.util.constants;

import java.util.Map;

public class StringUtils {

    private StringUtils() {}

    public static final String ADMIN_EMAIL = "moneyfi.owner@gmail.com";
    public static final String MESSAGE = "message";
    public static final Map<Integer, String> userRoleAssociation = Map.of(1, UserRoles.ADMIN.name(), 2, UserRoles.USER.name());

}
