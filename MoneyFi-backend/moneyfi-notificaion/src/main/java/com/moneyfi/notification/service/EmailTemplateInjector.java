package com.moneyfi.notification.service;

import com.moneyfi.notification.service.dto.NotificationQueueDto;
import com.moneyfi.notification.service.dto.emaildto.AdminBlockUserDto;
import com.moneyfi.notification.service.dto.emaildto.StatementAnalysisDto;
import com.moneyfi.notification.service.dto.emaildto.GmailSyncIncreaseRequestDto;
import com.moneyfi.notification.service.dto.emaildto.UserRaisedDefectDto;
import com.moneyfi.notification.service.email.EmailTemplates;
import com.moneyfi.notification.util.constants.StringConstants;
import com.moneyfi.notification.util.enums.NotificationQueueEnum;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EmailTemplateInjector {

    private EmailTemplateInjector() {}

    public static void functionToRouteBasedOnRequest(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        if (notificationQueueDto.getNotificationQueueType().equalsIgnoreCase(NotificationQueueEnum.USER_FEEDBACK_MAIL.name())) {
            functionCallForSendingFeedBackMailToAdmin(notificationQueueDto, emailTemplates);
        } else if (notificationQueueDto.getNotificationQueueType().equalsIgnoreCase(NotificationQueueEnum.USER_DEFECT_STATUS_MAIL.name())) {
            functionCallForSendingReportStatusToUser(notificationQueueDto, emailTemplates);
        } else if (notificationQueueDto.getNotificationQueueType().equalsIgnoreCase(NotificationQueueEnum.FORGOT_USERNAME_REQUEST_MAIL.name())) {
            functionCallForSendingUsernameToUser(notificationQueueDto, emailTemplates);
        } else if (notificationQueueDto.getNotificationQueueType().equalsIgnoreCase(NotificationQueueEnum.USER_RAISED_DEFECT_TO_ADMIN_MAIL.name())) {
            functionCallToSendUserRaisedDefectToAdmin(notificationQueueDto, emailTemplates);
        } else if (notificationQueueDto.getNotificationQueueType().equalsIgnoreCase(NotificationQueueEnum.SEND_REFERENCE_NUMBER_TO_USER_MAIL.name())) {
            functionCallToSendReferenceNumberToUser(notificationQueueDto, emailTemplates);
        } else if (notificationQueueDto.getNotificationQueueType().equalsIgnoreCase(NotificationQueueEnum.SEND_USER_REQUEST_TO_INCREASE_GMAIL_SYNC_COUNT_TO_ADMIN_MAIL.name())) {
            functionCallToSendGmailSyncCountIncreaseRequestToAdmin(notificationQueueDto, emailTemplates);
        } else if (notificationQueueDto.getNotificationQueueType().equalsIgnoreCase(NotificationQueueEnum.ACCOUNT_STATEMENT_EMAIL.name())) {
            functionCallToSendAccountStatementMailToUser(notificationQueueDto, emailTemplates);
        } else if (notificationQueueDto.getNotificationQueueType().equalsIgnoreCase(NotificationQueueEnum.SPENDING_ANALYSIS_EMAIL.name())) {
            functionCallToSendSpendingAnalysisMailToUser(notificationQueueDto, emailTemplates);
        } else if (notificationQueueDto.getNotificationQueueType().equalsIgnoreCase(NotificationQueueEnum.ADMIN_BLOCKED_USER_MAIL.name())) {
            functionCallToSendAdminBlockedUserAlertMailToUser(notificationQueueDto, emailTemplates);
        } else if (notificationQueueDto.getNotificationQueueType().equalsIgnoreCase(NotificationQueueEnum.GMAIL_SYNC_APPROVED_USER_MAIL.name())) {
            functionCallToSendGmailSyncCountIncreaseRequestApprovedMailToUser(notificationQueueDto, emailTemplates);
        } else if (notificationQueueDto.getNotificationQueueType().equalsIgnoreCase(NotificationQueueEnum.GMAIL_SYNC_REJECTED_USER_MAIL.name())) {
            functionCallToSendGmailSyncCountIncreaseRequestRejectedMailToUser(notificationQueueDto, emailTemplates);
        } else if (notificationQueueDto.getNotificationQueueType().equalsIgnoreCase(NotificationQueueEnum.CONTACT_US_HELP_CENTER_MAIL.name())) {
            functionCallToSendGmailToAdminThroughContactUsHepDesk(notificationQueueDto, emailTemplates);
        } else if (notificationQueueDto.getNotificationQueueType().equalsIgnoreCase(NotificationQueueEnum.NAME_CHANGE_REQUEST_APPROVED_MAIL.name())) {
            functionCallToSendMailForNameChangeRequestApprovedToUser(notificationQueueDto, emailTemplates);
        } else if (notificationQueueDto.getNotificationQueueType().equalsIgnoreCase(NotificationQueueEnum.NAME_CHANGE_REQUEST_REJECTED_MAIL.name())) {
            functionCallToSendMailForNameChangeRequestRejectedToUser(notificationQueueDto, emailTemplates);
        } else if (notificationQueueDto.getNotificationQueueType().equalsIgnoreCase(NotificationQueueEnum.USER_ACCOUNT_UNBLOCK_REQUEST_SUCCESSFUL_MAIL.name())
                    || notificationQueueDto.getNotificationQueueType().equalsIgnoreCase(NotificationQueueEnum.USER_ACCOUNT_RETRIEVAL_REQUEST_SUCCESSFUL_MAIL.name())) {
            functionCallToSendMailForAccountReactivateRequestApprovedToUser(notificationQueueDto, emailTemplates);
        } else if (notificationQueueDto.getNotificationQueueType().equalsIgnoreCase(NotificationQueueEnum.USER_ACCOUNT_UNBLOCK_REQUEST_REJECTED_MAIL.name())
                || notificationQueueDto.getNotificationQueueType().equalsIgnoreCase(NotificationQueueEnum.USER_ACCOUNT_RETRIEVAL_REQUEST_REJECTED_MAIL.name())) {
            functionCallToSendMailForAccountReactivateRequestRejectedToUser(notificationQueueDto, emailTemplates);
        } else if (notificationQueueDto.getNotificationQueueType().equalsIgnoreCase(NotificationQueueEnum.USER_PASSWORD_CHANGE_ALERT_MAIL.name())) {
            functionCallToSendMailForUserPasswordChange(notificationQueueDto, emailTemplates);
        } else if (notificationQueueDto.getNotificationQueueType().equalsIgnoreCase(NotificationQueueEnum.OTP_MAIL_FOR_USER_SIGNUP.name())) {
            functionCallToSendOtpForUserSignUp(notificationQueueDto, emailTemplates);
        } else if (notificationQueueDto.getNotificationQueueType().equalsIgnoreCase(NotificationQueueEnum.OTP_FOR_USER_BLOCK.name())) {
            functionCallToSendOtpUserBlock(notificationQueueDto, emailTemplates);
        } else if (notificationQueueDto.getNotificationQueueType().equalsIgnoreCase(NotificationQueueEnum.OTP_FOR_FORGOT_PASSWORD.name())) {
            functionCallToSendOtpWhenUserForgotPassword(notificationQueueDto, emailTemplates);
        } else if (notificationQueueDto.getNotificationQueueType().equalsIgnoreCase(NotificationQueueEnum.USER_ANNIVERSARY_MAIL.name())) {
            functionCallToSendUserAnniversaryMail(notificationQueueDto, emailTemplates);
        } else if (notificationQueueDto.getNotificationQueueType().equalsIgnoreCase(NotificationQueueEnum.USER_BIRTHDAY_MAIL.name())) {
            functionCallToSendUserBirthdayMail(notificationQueueDto, emailTemplates);
        }
    }

    private static void functionCallToSendUserBirthdayMail(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        String[] parts = notificationQueueDto.getValueJson().split(java.util.regex.Pattern.quote("<|>"));
        String email = parts[0];
        String name = parts[1];
        emailTemplates.sendBirthdayWishEmailToUsers(email, name);
    }

    private static void functionCallToSendUserAnniversaryMail(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        String[] parts = notificationQueueDto.getValueJson().split(java.util.regex.Pattern.quote("<|>"));
        String email = parts[0];
        String name = parts[1];
        int numberOfYears = Integer.parseInt(parts[2]);
        emailTemplates.sendAnniversaryCongratulationsMailToUser(email, name, numberOfYears);
    }

    private static void functionCallToSendOtpWhenUserForgotPassword(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        String[] parts = notificationQueueDto.getValueJson().split(java.util.regex.Pattern.quote("<|>"));
        String username = parts[0];
        String email = parts[1];
        String verificationCode = parts[2];
        emailTemplates.sendOtpForForgotPassword(username, email, verificationCode);
    }

    private static void functionCallToSendOtpUserBlock(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        String[] parts = notificationQueueDto.getValueJson().split(java.util.regex.Pattern.quote("<|>"));
        String username = parts[0];
        String name = parts[1];
        String verificationCode = parts[2];
        String type = parts[3];
        emailTemplates.sendOtpToUserForAccountBlock(username, name, verificationCode, type);
    }

    private static void functionCallToSendOtpForUserSignUp(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        String[] parts = notificationQueueDto.getValueJson().split(java.util.regex.Pattern.quote("<|>"));
        String email = parts[0];
        String name = parts[1];
        String verificationCode = parts[2];
        emailTemplates.sendOtpEmailToUserForSignup(name, email, verificationCode);
    }

    private static void functionCallToSendMailForUserPasswordChange(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        String[] parts = notificationQueueDto.getValueJson().split(java.util.regex.Pattern.quote("<|>"));
        String name = parts[0];
        String email = parts[1];
        emailTemplates.sendPasswordChangeAlertMail(name, email);
    }

    private static void functionCallToSendMailForAccountReactivateRequestRejectedToUser(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        String[] parts = notificationQueueDto.getValueJson().split(java.util.regex.Pattern.quote("<|>"));
        String name = parts[0];
        String email = parts[1];
        String referenceNumber = parts[2];
        String mode = parts[3];
        String declineReason = parts[4];
        emailTemplates.sendAccountUnblockOrRetrievalFailureMailToUser(name, email, referenceNumber, mode, declineReason);
    }

    private static void functionCallToSendMailForAccountReactivateRequestApprovedToUser(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        String[] parts = notificationQueueDto.getValueJson().split(java.util.regex.Pattern.quote("<|>"));
        String name = parts[0];
        String email = parts[1];
        String referenceNumber = parts[2];
        String mode = parts[3];
        emailTemplates.sendAccountUnblockOrRetrievalSuccessfulMailToUser(name, email, referenceNumber, mode);
    }

    private static void functionCallToSendMailForNameChangeRequestRejectedToUser(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        String[] parts = notificationQueueDto.getValueJson().split(java.util.regex.Pattern.quote("<|>"));
        String name = parts[0];
        String email = parts[1];
        String referenceNumber = parts[2];
        emailTemplates.sendNameChangeRequestRejectionMailToUser(name, email, referenceNumber);
    }

    private static void functionCallToSendMailForNameChangeRequestApprovedToUser(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        String[] parts = notificationQueueDto.getValueJson().split(java.util.regex.Pattern.quote("<|>"));
        String name = parts[0];
        String email = parts[1];
        String referenceNumber = parts[2];
        emailTemplates.sendNameChangeRequestApprovedMailToUser(name, email, referenceNumber);
    }

    private static void functionCallToSendGmailToAdminThroughContactUsHepDesk(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        String[] parts = notificationQueueDto.getValueJson().split(java.util.regex.Pattern.quote("<|>"));
        String email = parts[0];
        String phoneNumber = parts[1];
        String name = parts[2];
        String description = parts[3];
        emailTemplates.sendContactUsDetailsEmailToAdmin(email, phoneNumber, name, description);
    }

    private static void functionCallToSendGmailSyncCountIncreaseRequestRejectedMailToUser(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        String[] parts = notificationQueueDto.getValueJson().split(java.util.regex.Pattern.quote("<|>"));
        String name = parts[0];
        String email = parts[1];
        emailTemplates.sendUserGmailSyncCountIncreaseRequestRejection(name, email);
    }

    private static void functionCallToSendGmailSyncCountIncreaseRequestApprovedMailToUser(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        String[] parts = notificationQueueDto.getValueJson().split(java.util.regex.Pattern.quote("<|>"));
        String name = parts[0];
        String email = parts[1];
        int gmailSyncRequestCount = Integer.parseInt(parts[2]);
        emailTemplates.sendUserGmailSyncApprovedMail(name, email, gmailSyncRequestCount);
    }

    private static void functionCallToSendAdminBlockedUserAlertMailToUser(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        AdminBlockUserDto adminBlockUserDto = StringConstants.objectMapper.readValue(notificationQueueDto.getValueJson(), AdminBlockUserDto.class);
        emailTemplates.sendBlockAlertMailToUser(adminBlockUserDto.getEmail(), adminBlockUserDto.getReason(), adminBlockUserDto.getName(), adminBlockUserDto.getFile());
    }

    private static void functionCallToSendSpendingAnalysisMailToUser(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        StatementAnalysisDto statementAnalysisDto = StringConstants.objectMapper.readValue(notificationQueueDto.getValueJson(), StatementAnalysisDto.class);
        emailTemplates.sendSpendingAnalysisEmail(statementAnalysisDto);
    }

    private static void functionCallToSendAccountStatementMailToUser(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        StatementAnalysisDto statementAnalysisDto = StringConstants.objectMapper.readValue(notificationQueueDto.getValueJson(), StatementAnalysisDto.class);
        emailTemplates.sendAccountStatementAsEmail(statementAnalysisDto);
    }

    private static void functionCallToSendGmailSyncCountIncreaseRequestToAdmin(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            GmailSyncIncreaseRequestDto gmailSyncIncreaseRequestDto = StringConstants.objectMapper.readValue(notificationQueueDto.getValueJson(), GmailSyncIncreaseRequestDto.class);
            emailTemplates.sendUserRaisedGmailSyncRequestEmailToAdmin(gmailSyncIncreaseRequestDto);
        } catch (Exception e) {
            log.error("Failed to process SEND_USER_REQUEST_TO_INCREASE_GMAIL_SYNC_COUNT_TO_ADMIN_MAIL: {}", notificationQueueDto.getValueJson(), e);
        }
    }

    private static void functionCallToSendReferenceNumberToUser(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        String[] parts = notificationQueueDto.getValueJson().split(java.util.regex.Pattern.quote("<|>"));
        String name = parts[0];
        String email = parts[1];
        String description = parts[2];
        String referenceNumber = parts[3];
        emailTemplates.sendReferenceNumberEmailToUser(name, email, description, referenceNumber);
    }

    private static void functionCallToSendUserRaisedDefectToAdmin(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            UserRaisedDefectDto userRaisedDefectDto = StringConstants.objectMapper.readValue(notificationQueueDto.getValueJson(), UserRaisedDefectDto.class);
            emailTemplates.sendUserRaiseDefectEmailToAdmin(userRaisedDefectDto);
        } catch (Exception e) {
            log.error("Failed to process USER_RAISED_DEFECT_TO_ADMIN_MAIL: {}", notificationQueueDto.getValueJson(), e);
        }
    }

    private static void functionCallForSendingUsernameToUser(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        String username = notificationQueueDto.getValueJson();
        emailTemplates.sendUserNameToUser(username);
    }

    private static void functionCallForSendingReportStatusToUser(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        String[] parts = notificationQueueDto.getValueJson().split(java.util.regex.Pattern.quote("<|>"));
        String name = parts[0];
        String referenceNumber = parts[1];
        String description = parts[2];
        String email = parts[3];
        emailTemplates.sendUserReportStatusMailToUser(name, referenceNumber, description, email);
    }

    private static void functionCallForSendingFeedBackMailToAdmin(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        String[] parts = notificationQueueDto.getValueJson().split(java.util.regex.Pattern.quote("<|>"));
        String rating = parts[0];
        String message = parts[1];
        emailTemplates.sendUserFeedbackEmailToAdmin(rating, message);
    }
}
