package com.moneyfi.constants.constants;

import static com.moneyfi.constants.constants.CommonConstants.SLASH;

public class BasePathConstants {

    private BasePathConstants() {}

    private static final String API = "api";
    private static final String VERSION_1 = "v1";

    public static final String ACTUATOR = "/actuator";
    public static final String TRANSACTION_SERVICE_BASE_PATH = SLASH + API + SLASH + VERSION_1 + SLASH + "transaction";
    public static final String WEALTH_CORE_SERVICE_BASE_PATH = SLASH + API + SLASH + VERSION_1 + SLASH + "wealth-core";
    public static final String USER_SERVICE_BASE_PATH = SLASH + API + SLASH + VERSION_1 + SLASH + "user-service";
    public static final String[] SWAGGER_WHITELIST = {
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            TRANSACTION_SERVICE_BASE_PATH + "/v3/api-docs/**",
            WEALTH_CORE_SERVICE_BASE_PATH + "/v3/api-docs/**",
            USER_SERVICE_BASE_PATH + "/v3/api-docs/**",
    };
}
