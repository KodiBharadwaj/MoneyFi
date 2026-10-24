package com.moneyfi.notification.service;

import com.moneyfi.constants.dto.BatchInfoForEmailDto;
import com.moneyfi.constants.enums.NotificationQueueEnum;
import com.moneyfi.notification.service.artemis.producer.ArtemisQueueProducer;
import com.moneyfi.notification.service.dto.NotificationQueueDto;
import com.moneyfi.notification.service.dto.emaildto.AdminBlockUserDto;
import com.moneyfi.notification.service.dto.emaildto.StatementAnalysisDto;
import com.moneyfi.notification.service.dto.emaildto.GmailSyncIncreaseRequestDto;
import com.moneyfi.notification.service.dto.emaildto.UserRaisedDefectDto;
import com.moneyfi.notification.service.email.EmailTemplates;
import com.moneyfi.notification.util.constants.StringConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.thirdparty.jackson.core.JsonProcessingException;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;

import java.util.List;

import static com.moneyfi.constants.constants.CommonConstants.STRING_SEPARATOR;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailTemplateInjector {

    private final ArtemisQueueProducer artemisQueueProducer;

    public void functionToRouteBasedOnRequest(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) throws JsonProcessingException {
        switch (NotificationQueueEnum.valueOf(notificationQueueDto.getNotificationQueueType())) {
            case USER_FEEDBACK_MAIL ->
                    functionCallForSendingFeedBackMailToAdmin(notificationQueueDto, emailTemplates);
            case USER_DEFECT_STATUS_MAIL ->
                    functionCallForSendingReportStatusToUser(notificationQueueDto, emailTemplates);
            case FORGOT_USERNAME_REQUEST_MAIL ->
                    functionCallForSendingUsernameToUser(notificationQueueDto, emailTemplates);
            case USER_RAISED_DEFECT_TO_ADMIN_MAIL ->
                    functionCallToSendUserRaisedDefectToAdmin(notificationQueueDto, emailTemplates);
            case SEND_REFERENCE_NUMBER_TO_USER_MAIL ->
                    functionCallToSendReferenceNumberToUser(notificationQueueDto, emailTemplates);
            case SEND_USER_REQUEST_TO_INCREASE_GMAIL_SYNC_COUNT_TO_ADMIN_MAIL ->
                    functionCallToSendGmailSyncCountIncreaseRequestToAdmin(notificationQueueDto, emailTemplates);
            case ACCOUNT_STATEMENT_EMAIL ->
                    functionCallToSendAccountStatementMailToUser(notificationQueueDto, emailTemplates);
            case SPENDING_ANALYSIS_EMAIL ->
                    functionCallToSendSpendingAnalysisMailToUser(notificationQueueDto, emailTemplates);
            case ADMIN_BLOCKED_USER_MAIL ->
                    functionCallToSendAdminBlockedUserAlertMailToUser(notificationQueueDto, emailTemplates);
            case GMAIL_SYNC_APPROVED_USER_MAIL ->
                    functionCallToSendGmailSyncCountIncreaseRequestApprovedMailToUser(notificationQueueDto, emailTemplates);
            case GMAIL_SYNC_REJECTED_USER_MAIL ->
                    functionCallToSendGmailSyncCountIncreaseRequestRejectedMailToUser(notificationQueueDto, emailTemplates);
            case CONTACT_US_HELP_CENTER_MAIL ->
                    functionCallToSendGmailToAdminThroughContactUsHepDesk(notificationQueueDto, emailTemplates);
            case NAME_CHANGE_REQUEST_APPROVED_MAIL ->
                    functionCallToSendMailForNameChangeRequestApprovedToUser(notificationQueueDto, emailTemplates);
            case NAME_CHANGE_REQUEST_REJECTED_MAIL ->
                    functionCallToSendMailForNameChangeRequestRejectedToUser(notificationQueueDto, emailTemplates);
            case USER_ACCOUNT_UNBLOCK_REQUEST_SUCCESSFUL_MAIL, USER_ACCOUNT_RETRIEVAL_REQUEST_SUCCESSFUL_MAIL ->
                    functionCallToSendMailForAccountReactivateRequestApprovedToUser(notificationQueueDto, emailTemplates);
            case USER_ACCOUNT_UNBLOCK_REQUEST_REJECTED_MAIL, USER_ACCOUNT_RETRIEVAL_REQUEST_REJECTED_MAIL ->
                    functionCallToSendMailForAccountReactivateRequestRejectedToUser(notificationQueueDto, emailTemplates);
            case USER_PASSWORD_CHANGE_ALERT_MAIL ->
                    functionCallToSendMailForUserPasswordChange(notificationQueueDto, emailTemplates);
            case OTP_MAIL_FOR_USER_SIGNUP ->
                    functionCallToSendOtpForUserSignUp(notificationQueueDto, emailTemplates);
            case OTP_FOR_USER_BLOCK ->
                    functionCallToSendOtpUserBlock(notificationQueueDto, emailTemplates);
            case OTP_FOR_FORGOT_PASSWORD ->
                    functionCallToSendOtpWhenUserForgotPassword(notificationQueueDto, emailTemplates);
            case USER_ANNIVERSARY_MAIL ->
                    functionCallToSendUserAnniversaryMail(notificationQueueDto, emailTemplates);
            case USER_BIRTHDAY_MAIL ->
                    functionCallToSendUserBirthdayMail(notificationQueueDto, emailTemplates);
            case USER_ACCOUNT_CREATION_MAIL ->
                    functionCallToSendUserAccountCreationMail(notificationQueueDto, emailTemplates);
            case USER_BATCH_INFO_EMAIL ->
                    functionCallToSendBatchInfoEmailToUser(notificationQueueDto, emailTemplates);
            default -> log.error("Incorrect type");
        }
    }

    private void functionCallToSendBatchInfoEmailToUser(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) throws JsonProcessingException {
        try {
            List<BatchInfoForEmailDto> batchInfoList = StringConstants.objectMapper.readValue(notificationQueueDto.getValueJson(), new TypeReference<List<BatchInfoForEmailDto>>() {});
            emailTemplates.sendEmailForSuccessfulBatchTransactionInsertion(batchInfoList);
        } catch (JacksonException e) {
            e.printStackTrace();
            artemisQueueProducer.sendFeedbackMessage(e.getMessage());
        }
    }

    private void functionCallToSendUserAccountCreationMail(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            String[] parts = notificationQueueDto.getValueJson().split(java.util.regex.Pattern.quote(STRING_SEPARATOR));
            String email = parts[0];
            String name = parts[1];
            emailTemplates.sendEmailForSuccessfulUserCreation(email, name);
        } catch (Exception e) {
            e.printStackTrace();
            artemisQueueProducer.sendFeedbackMessage(e.getMessage());
        }
    }

    private void functionCallToSendUserBirthdayMail(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            String[] parts = notificationQueueDto.getValueJson().split(java.util.regex.Pattern.quote(STRING_SEPARATOR));
            String email = parts[0];
            String name = parts[1];
            emailTemplates.sendBirthdayWishEmailToUsers(email, name);
        } catch (Exception e) {
            e.printStackTrace();
            artemisQueueProducer.sendFeedbackMessage(e.getMessage());
        }
    }

    private void functionCallToSendUserAnniversaryMail(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            String[] parts = notificationQueueDto.getValueJson().split(java.util.regex.Pattern.quote(STRING_SEPARATOR));
            String email = parts[0];
            String name = parts[1];
            int numberOfYears = Integer.parseInt(parts[2]);
            emailTemplates.sendAnniversaryCongratulationsMailToUser(email, name, numberOfYears);
        } catch (Exception e) {
            e.printStackTrace();
            artemisQueueProducer.sendFeedbackMessage(e.getMessage());
        }
    }

    private void functionCallToSendOtpWhenUserForgotPassword(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            String[] parts = notificationQueueDto.getValueJson().split(java.util.regex.Pattern.quote(STRING_SEPARATOR));
            String username = parts[0];
            String email = parts[1];
            String verificationCode = parts[2];
            emailTemplates.sendOtpForForgotPassword(username, email, verificationCode);
        } catch (Exception e) {
            e.printStackTrace();
            artemisQueueProducer.sendFeedbackMessage(e.getMessage());
        }
    }

    private void functionCallToSendOtpUserBlock(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            String[] parts = notificationQueueDto.getValueJson().split(java.util.regex.Pattern.quote(STRING_SEPARATOR));
            String username = parts[0];
            String name = parts[1];
            String verificationCode = parts[2];
            String type = parts[3];
            emailTemplates.sendOtpToUserForAccountBlock(username, name, verificationCode, type);
        } catch (Exception e) {
            e.printStackTrace();
            artemisQueueProducer.sendFeedbackMessage(e.getMessage());
        }
    }

    private void functionCallToSendOtpForUserSignUp(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            String[] parts = notificationQueueDto.getValueJson().split(java.util.regex.Pattern.quote(STRING_SEPARATOR));
            String email = parts[0];
            String name = parts[1];
            String verificationCode = parts[2];
            emailTemplates.sendOtpEmailToUserForSignup(email, name, verificationCode);
        } catch (Exception e) {
            e.printStackTrace();
            artemisQueueProducer.sendFeedbackMessage(e.getMessage());
        }
    }

    private void functionCallToSendMailForUserPasswordChange(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            String[] parts = notificationQueueDto.getValueJson().split(java.util.regex.Pattern.quote(STRING_SEPARATOR));
            String name = parts[0];
            String email = parts[1];
            emailTemplates.sendPasswordChangeAlertMail(name, email);
        } catch (Exception e) {
            e.printStackTrace();
            artemisQueueProducer.sendFeedbackMessage(e.getMessage());
        }
    }

    private void functionCallToSendMailForAccountReactivateRequestRejectedToUser(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            String[] parts = notificationQueueDto.getValueJson().split(java.util.regex.Pattern.quote(STRING_SEPARATOR));
            String name = parts[0];
            String email = parts[1];
            String referenceNumber = parts[2];
            String mode = parts[3];
            String declineReason = parts[4];
            emailTemplates.sendAccountUnblockOrRetrievalFailureMailToUser(name, email, referenceNumber, mode, declineReason);
        } catch (Exception e) {
            e.printStackTrace();
            artemisQueueProducer.sendFeedbackMessage(e.getMessage());
        }
    }

    private void functionCallToSendMailForAccountReactivateRequestApprovedToUser(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            String[] parts = notificationQueueDto.getValueJson().split(java.util.regex.Pattern.quote(STRING_SEPARATOR));
            String name = parts[0];
            String email = parts[1];
            String referenceNumber = parts[2];
            String mode = parts[3];
            emailTemplates.sendAccountUnblockOrRetrievalSuccessfulMailToUser(name, email, referenceNumber, mode);
        } catch (Exception e) {
            e.printStackTrace();
            artemisQueueProducer.sendFeedbackMessage(e.getMessage());
        }
    }

    private void functionCallToSendMailForNameChangeRequestRejectedToUser(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            String[] parts = notificationQueueDto.getValueJson().split(java.util.regex.Pattern.quote(STRING_SEPARATOR));
            String name = parts[0];
            String email = parts[1];
            String referenceNumber = parts[2];
            emailTemplates.sendNameChangeRequestRejectionMailToUser(name, email, referenceNumber);
        } catch (Exception e) {
            e.printStackTrace();
            artemisQueueProducer.sendFeedbackMessage(e.getMessage());
        }
    }

    private void functionCallToSendMailForNameChangeRequestApprovedToUser(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            String[] parts = notificationQueueDto.getValueJson().split(java.util.regex.Pattern.quote(STRING_SEPARATOR));
            String name = parts[0];
            String email = parts[1];
            String referenceNumber = parts[2];
            emailTemplates.sendNameChangeRequestApprovedMailToUser(name, email, referenceNumber);
        } catch (Exception e) {
            e.printStackTrace();
            artemisQueueProducer.sendFeedbackMessage(e.getMessage());
        }
    }

    private void functionCallToSendGmailToAdminThroughContactUsHepDesk(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            String[] parts = notificationQueueDto.getValueJson().split(java.util.regex.Pattern.quote(STRING_SEPARATOR));
            String email = parts[0];
            String phoneNumber = parts[1];
            String name = parts[2];
            String description = parts[3];
            emailTemplates.sendContactUsDetailsEmailToAdmin(email, phoneNumber, name, description);
        } catch (Exception e) {
            e.printStackTrace();
            artemisQueueProducer.sendFeedbackMessage(e.getMessage());
        }
    }

    private void functionCallToSendGmailSyncCountIncreaseRequestRejectedMailToUser(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            String[] parts = notificationQueueDto.getValueJson().split(java.util.regex.Pattern.quote(STRING_SEPARATOR));
            String name = parts[0];
            String email = parts[1];
            emailTemplates.sendUserGmailSyncCountIncreaseRequestRejection(name, email);
        } catch (Exception e) {
            e.printStackTrace();
            artemisQueueProducer.sendFeedbackMessage(e.getMessage());
        }
    }

    private void functionCallToSendGmailSyncCountIncreaseRequestApprovedMailToUser(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            String[] parts = notificationQueueDto.getValueJson().split(java.util.regex.Pattern.quote(STRING_SEPARATOR));
            String name = parts[0];
            String email = parts[1];
            int gmailSyncRequestCount = Integer.parseInt(parts[2]);
            emailTemplates.sendUserGmailSyncApprovedMail(name, email, gmailSyncRequestCount);
        } catch (Exception e) {
            e.printStackTrace();
            artemisQueueProducer.sendFeedbackMessage(e.getMessage());
        }
    }

    private void functionCallToSendAdminBlockedUserAlertMailToUser(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            AdminBlockUserDto adminBlockUserDto = StringConstants.objectMapper.readValue(notificationQueueDto.getValueJson(), AdminBlockUserDto.class);
            emailTemplates.sendBlockAlertMailToUser(adminBlockUserDto.getEmail(), adminBlockUserDto.getReason(), adminBlockUserDto.getName(), adminBlockUserDto.getFile());
        } catch (Exception e) {
            e.printStackTrace();
            artemisQueueProducer.sendFeedbackMessage(e.getMessage());
        }
    }

    private void functionCallToSendSpendingAnalysisMailToUser(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            StatementAnalysisDto statementAnalysisDto = StringConstants.objectMapper.readValue(notificationQueueDto.getValueJson(), StatementAnalysisDto.class);
            emailTemplates.sendSpendingAnalysisEmail(statementAnalysisDto);
        } catch (Exception e) {
            e.printStackTrace();
            artemisQueueProducer.sendFeedbackMessage(e.getMessage());
        }
    }

    private void functionCallToSendAccountStatementMailToUser(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            StatementAnalysisDto statementAnalysisDto = StringConstants.objectMapper.readValue(notificationQueueDto.getValueJson(), StatementAnalysisDto.class);
            emailTemplates.sendAccountStatementAsEmail(statementAnalysisDto);
        } catch (Exception e) {
            e.printStackTrace();
            artemisQueueProducer.sendFeedbackMessage(e.getMessage());
        }
    }

    private void functionCallToSendGmailSyncCountIncreaseRequestToAdmin(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            GmailSyncIncreaseRequestDto gmailSyncIncreaseRequestDto = StringConstants.objectMapper.readValue(notificationQueueDto.getValueJson(), GmailSyncIncreaseRequestDto.class);
            emailTemplates.sendUserRaisedGmailSyncRequestEmailToAdmin(gmailSyncIncreaseRequestDto);
        } catch (Exception e) {
            e.printStackTrace();
            artemisQueueProducer.sendFeedbackMessage(e.getMessage());
        }
    }

    private void functionCallToSendReferenceNumberToUser(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            String[] parts = notificationQueueDto.getValueJson().split(java.util.regex.Pattern.quote(STRING_SEPARATOR));
            String name = parts[0];
            String email = parts[1];
            String description = parts[2];
            String referenceNumber = parts[3];
            emailTemplates.sendReferenceNumberEmailToUser(name, email, description, referenceNumber);
        } catch (Exception e) {
            e.printStackTrace();
            artemisQueueProducer.sendFeedbackMessage(e.getMessage());
        }
    }

    private void functionCallToSendUserRaisedDefectToAdmin(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            UserRaisedDefectDto userRaisedDefectDto = StringConstants.objectMapper.readValue(notificationQueueDto.getValueJson(), UserRaisedDefectDto.class);
            emailTemplates.sendUserRaiseDefectEmailToAdmin(userRaisedDefectDto);
        } catch (Exception e) {
            e.printStackTrace();
            artemisQueueProducer.sendFeedbackMessage(e.getMessage());
        }
    }

    private void functionCallForSendingUsernameToUser(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            String username = notificationQueueDto.getValueJson();
            emailTemplates.sendUserNameToUser(username);
        } catch (Exception e) {
            e.printStackTrace();
            artemisQueueProducer.sendFeedbackMessage(e.getMessage());
        }
    }

    private void functionCallForSendingReportStatusToUser(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            String[] parts = notificationQueueDto.getValueJson().split(java.util.regex.Pattern.quote(STRING_SEPARATOR));
            String name = parts[0];
            String referenceNumber = parts[1];
            String description = parts[2];
            String email = parts[3];
            emailTemplates.sendUserReportStatusMailToUser(name, referenceNumber, description, email);
        } catch (Exception e) {
            e.printStackTrace();
            artemisQueueProducer.sendFeedbackMessage(e.getMessage());
        }
    }

    private void functionCallForSendingFeedBackMailToAdmin(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            String[] parts = notificationQueueDto.getValueJson().split(java.util.regex.Pattern.quote(STRING_SEPARATOR));
            String rating = parts[0];
            String message = parts[1];
            emailTemplates.sendUserFeedbackEmailToAdmin(rating, message);
        } catch (Exception e) {
            e.printStackTrace();
            artemisQueueProducer.sendFeedbackMessage(e.getMessage());
        }
    }
}
