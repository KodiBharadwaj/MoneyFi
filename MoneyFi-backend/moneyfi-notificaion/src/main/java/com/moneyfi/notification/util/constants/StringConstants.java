package com.moneyfi.notification.util.constants;

import tools.jackson.databind.ObjectMapper;

public class StringConstants {

    private StringConstants() {}

    public static final ObjectMapper objectMapper = new ObjectMapper();

    public static final String EMAIL_SENT_SUCCESS_MESSAGE = "Email sent successfully!";
}
