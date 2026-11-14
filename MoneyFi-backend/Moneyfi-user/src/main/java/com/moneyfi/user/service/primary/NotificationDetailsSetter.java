package com.moneyfi.user.service.primary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.moneyfi.user.service.primary.general.inter.EmailNotificationFailedService;
import com.moneyfi.user.util.enums.QueueFailureStage;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.Map;

import static com.moneyfi.constants.constants.CommonConstants.*;
import static com.moneyfi.constants.constants.CommonConstants.MDC_REQUEST_ID;

@Slf4j
@UtilityClass
public class NotificationDetailsSetter {

    public static Map<String, Object> setUserDetailsForNotificationQueue(String email, String name, String verificationCode, String notificationType, EmailNotificationFailedService emailNotificationFailedService) throws JsonProcessingException {
        var details = new java.util.HashMap<String, Object>();
        details.put(STRING_USER_NAME, name);
        details.put(STRING_USERNAME, email);
        details.put(STRING_VERIFICATION_CODE, verificationCode);
        return buildNotificationDetails(details, email, notificationType, emailNotificationFailedService);
    }

    public static Map<String, Object> setUserDetailsForNotificationQueue(String email, String name, String verificationCode, String type, String notificationType, EmailNotificationFailedService emailNotificationFailedService) throws JsonProcessingException {
        var details = new java.util.HashMap<String, Object>();
        details.put(STRING_USER_NAME, name);
        details.put(STRING_USERNAME, email);
        details.put(STRING_VERIFICATION_CODE, verificationCode);
        details.put(STRING_TYPE, type);
        return buildNotificationDetails(details, email, notificationType, emailNotificationFailedService);
    }

    public static Map<String, Object> setUserDetailsForNotificationQueue(String email, String name, String notificationType, EmailNotificationFailedService emailNotificationFailedService) throws JsonProcessingException {
        var details = new java.util.HashMap<String, Object>();
        details.put(STRING_USER_NAME, name);
        details.put(STRING_USERNAME, email);
        return buildNotificationDetails(details, email, notificationType, emailNotificationFailedService);
    }

    public static Map<String, Object> setUserDetailsForNotificationQueueWithReferenceNumber(String email, String name, String referenceNumber, String message, String notificationType, EmailNotificationFailedService emailNotificationFailedService) throws JsonProcessingException {
        var details = new java.util.HashMap<String, Object>();
        details.put(STRING_USER_NAME, name);
        details.put(STRING_USERNAME, email);
        details.put(STRING_REFERENCE_NUMBER, referenceNumber);
        details.put(STRING_MESSAGE, message);
        return buildNotificationDetails(details, email, notificationType, emailNotificationFailedService);
    }

    public static Map<String, Object> setUserDetailsForNotificationQueueWithReferenceNumber(String email, String name, String referenceNumber, String message, String reason, String notificationType, EmailNotificationFailedService emailNotificationFailedService) throws JsonProcessingException {
        var details = new java.util.HashMap<String, Object>();
        details.put(STRING_USER_NAME, name);
        details.put(STRING_USERNAME, email);
        details.put(STRING_REFERENCE_NUMBER, referenceNumber);
        details.put(STRING_MESSAGE, message);
        details.put(STRING_REASON, reason);
        return buildNotificationDetails(details, email, notificationType, emailNotificationFailedService);
    }

    public static Map<String, Object> setUserDetailsForNotificationQueueWithReferenceNumber(String email, String name, String referenceNumber, String notificationType, EmailNotificationFailedService emailNotificationFailedService) throws JsonProcessingException {
        var details = new java.util.HashMap<String, Object>();
        details.put(STRING_USER_NAME, name);
        details.put(STRING_USERNAME, email);
        details.put(STRING_REFERENCE_NUMBER, referenceNumber);
        return buildNotificationDetails(details, email, notificationType, emailNotificationFailedService);
    }

    public static Map<String, Object> setUserDetailsForNotificationQueueForRating(String rating, String message, String notificationType, EmailNotificationFailedService emailNotificationFailedService) throws JsonProcessingException {
        var details = new java.util.HashMap<String, Object>();
        details.put(STRING_RATING, rating);
        details.put(STRING_MESSAGE, message);
        return buildNotificationDetails(details, null, notificationType, emailNotificationFailedService);
    }

    public static Map<String, Object> setUserDetailsForNotificationQueueWithReferenceNumber(String email, String name, int gmailSyncRequestCount, String notificationType, EmailNotificationFailedService emailNotificationFailedService) throws JsonProcessingException {
        var details = new java.util.HashMap<String, Object>();
        details.put(STRING_USER_NAME, name);
        details.put(STRING_USERNAME, email);
        details.put(STRING_GMAIL_SYNC_REQUEST_COUNT, gmailSyncRequestCount);
        return buildNotificationDetails(details, email, notificationType, emailNotificationFailedService);
    }

    public static Map<String, Object> setUserDetailsForNotificationQueueHelpDesk(String email, String name, String phoneNumber, String description, String notificationType, EmailNotificationFailedService emailNotificationFailedService) throws JsonProcessingException {
        var details = new java.util.HashMap<String, Object>();
        details.put(STRING_USER_NAME, name);
        details.put(STRING_USERNAME, email);
        details.put(STRING_PHONE_NUMBER, phoneNumber);
        details.put(STRING_DESCRIPTION, description);
        return buildNotificationDetails(details, email, notificationType, emailNotificationFailedService);
    }

    public static Map<String, Object> setUserDetailsForNotificationQueueAdminServiceForSendingAnniversary(String email, String name, int numberOfYears, String notificationType, EmailNotificationFailedService emailNotificationFailedService) throws JsonProcessingException {
        var details = new java.util.HashMap<String, Object>();
        details.put(STRING_USER_NAME, name);
        details.put(STRING_USERNAME, email);
        details.put(STRING_NUMBER_OF_YEARS, numberOfYears);
        return buildNotificationDetails(details, email, notificationType, emailNotificationFailedService);
    }

    private static Map<String, Object> buildNotificationDetails(Map<String, Object> details, String email, String notificationType, EmailNotificationFailedService emailNotificationFailedService) throws JsonProcessingException {
        try {
            details.put(STRING_MDC_REQUEST_ID, MDC.get(MDC_REQUEST_ID));
            return details;
        } catch (Exception e) {
            log.error("Error occurred: {}", e.getMessage());
            if (email != null) {
                emailNotificationFailedService.saveFailedEmailNotificationDetails(email, e.getMessage(), QueueFailureStage.SOURCE_SERVICE.name(), notificationType);
            }
            throw new RuntimeException(e);
        }
    }
}
