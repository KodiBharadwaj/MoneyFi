package com.moneyfi.apigateway.util.constants;

public class StringUrls {

    private StringUrls() {
    }

    public static final String GOOGLE_TOKEN_END_POINT_URL = "https://oauth2.googleapis.com/token";
    public static final String GOOGLE_TOKEN_EXTRA_SECURITY_CHECK_URL = "https://www.googleapis.com/oauth2/v3/tokeninfo?access_token={token}";
    public static final String GOOGLE_GMAIL_READONLY_CHECK_URL = "https://www.googleapis.com/auth/gmail.readonly";
    public static final String GOOGLE_USER_INFO_GET_URL = "https://openidconnect.googleapis.com/v1/userinfo";

    public static final String USER_SERVICE_OPEN_URL = "http://MONEYFI-USER/api/v1/user-service/open";
}
