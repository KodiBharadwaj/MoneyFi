package com.moneyfi.notification.service;

import com.moneyfi.constants.dto.BatchInfoForEmailDto;
import com.moneyfi.constants.dto.FeedbackResponseDto;
import com.moneyfi.constants.dto.NotificationQueueDto;
import com.moneyfi.constants.enums.NotificationQueueEnum;
import com.moneyfi.notification.service.dto.emaildto.AdminBlockUserDto;
import com.moneyfi.notification.service.dto.emaildto.StatementAnalysisDto;
import com.moneyfi.notification.service.dto.emaildto.GmailSyncIncreaseRequestDto;
import com.moneyfi.notification.service.dto.emaildto.UserRaisedDefectDto;
import com.moneyfi.notification.service.email.EmailTemplates;
import com.moneyfi.notification.service.event.publisher.EventPublisherService;
import com.moneyfi.notification.util.constants.StringConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.thirdparty.jackson.core.JsonProcessingException;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static com.moneyfi.constants.constants.CommonConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailTemplateInjector {
    
    private final EventPublisherService eventPublisherService;
    private final ObjectMapper objectMapper;

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
            log.error("Notification failed with error: {}", e.getMessage());
            eventPublisherService.publishMessage(FeedbackResponseDto.builder()
                    .requestValueJson(notificationQueueDto)
                    .errorMessage(String.valueOf(e.getMessage()))
                    .build()
            );
        }
    }

    private void functionCallToSendUserAccountCreationMail(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            JsonNode jsonNode = parseJson(notificationQueueDto);
            emailTemplates.sendEmailForSuccessfulUserCreation(
                    getFieldValue(jsonNode, STRING_USERNAME),
                    getFieldValue(jsonNode, STRING_USER_NAME)
            );
        } catch (Exception e) {
            log.error("Notification failed with error: {}", e.getMessage());
            eventPublisherService.publishMessage(FeedbackResponseDto.builder()
                    .requestValueJson(notificationQueueDto)
                    .errorMessage(String.valueOf(e.getMessage()))
                    .build()
            );
        }
    }

    private void functionCallToSendUserBirthdayMail(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            JsonNode jsonNode = parseJson(notificationQueueDto);
            emailTemplates.sendBirthdayWishEmailToUsers(
                    getFieldValue(jsonNode, STRING_USERNAME),
                    getFieldValue(jsonNode, STRING_USER_NAME)
            );
        } catch (Exception e) {
            log.error("Notification failed with error: {}", e.getMessage());
            eventPublisherService.publishMessage(FeedbackResponseDto.builder()
                    .requestValueJson(notificationQueueDto)
                    .errorMessage(String.valueOf(e.getMessage()))
                    .build()
            );
        }
    }

    private void functionCallToSendUserAnniversaryMail(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            JsonNode jsonNode = parseJson(notificationQueueDto);
            emailTemplates.sendAnniversaryCongratulationsMailToUser(
                    getFieldValue(jsonNode, STRING_USERNAME),
                    getFieldValue(jsonNode, STRING_USER_NAME),
                    Integer.parseInt(getFieldValue(jsonNode, STRING_NUMBER_OF_YEARS))
            );
        } catch (Exception e) {
            log.error("Notification failed with error: {}", e.getMessage());
            eventPublisherService.publishMessage(FeedbackResponseDto.builder()
                    .requestValueJson(notificationQueueDto)
                    .errorMessage(String.valueOf(e.getMessage()))
                    .build()
            );
        }
    }

    private void functionCallToSendOtpWhenUserForgotPassword(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            JsonNode jsonNode = parseJson(notificationQueueDto);
            emailTemplates.sendOtpForForgotPassword(
                    getFieldValue(jsonNode, STRING_USERNAME),
                    getFieldValue(jsonNode, STRING_USER_NAME),
                    getFieldValue(jsonNode, STRING_VERIFICATION_CODE)
            );
        } catch (Exception e) {
            log.error("Notification failed with error: {}", e.getMessage());
            eventPublisherService.publishMessage(FeedbackResponseDto.builder()
                    .requestValueJson(notificationQueueDto)
                    .errorMessage(String.valueOf(e.getMessage()))
                    .build()
            );
        }
    }

    private void functionCallToSendOtpUserBlock(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            JsonNode jsonNode = parseJson(notificationQueueDto);
            emailTemplates.sendOtpToUserForAccountBlock(
                    getFieldValue(jsonNode, STRING_USERNAME),
                    getFieldValue(jsonNode, STRING_USER_NAME),
                    getFieldValue(jsonNode, STRING_VERIFICATION_CODE),
                    getFieldValue(jsonNode, STRING_TYPE)
            );
        } catch (Exception e) {
            log.error("Notification failed with error: {}", e.getMessage());
            eventPublisherService.publishMessage(FeedbackResponseDto.builder()
                    .requestValueJson(notificationQueueDto)
                    .errorMessage(String.valueOf(e.getMessage()))
                    .build()
            );
        }
    }

    private void functionCallToSendOtpForUserSignUp(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            JsonNode jsonNode = parseJson(notificationQueueDto);
            emailTemplates.sendOtpEmailToUserForSignup(
                    getFieldValue(jsonNode, STRING_USERNAME),
                    getFieldValue(jsonNode, STRING_USER_NAME),
                    getFieldValue(jsonNode, STRING_VERIFICATION_CODE)
            );
        } catch (Exception e) {
            log.error("Notification failed with error: {}", e.getMessage());
            eventPublisherService.publishMessage(FeedbackResponseDto.builder()
                    .requestValueJson(notificationQueueDto)
                    .errorMessage(String.valueOf(e.getMessage()))
                    .build()
            );
        }
    }

    private void functionCallToSendMailForUserPasswordChange(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            JsonNode jsonNode = parseJson(notificationQueueDto);
            emailTemplates.sendPasswordChangeAlertMail(
                    getFieldValue(jsonNode, STRING_USERNAME),
                    getFieldValue(jsonNode, STRING_USER_NAME)
            );
        } catch (Exception e) {
            log.error("Notification failed with error: {}", e.getMessage());
            eventPublisherService.publishMessage(FeedbackResponseDto.builder()
                    .requestValueJson(notificationQueueDto)
                    .errorMessage(String.valueOf(e.getMessage()))
                    .build()
            );
        }
    }

    private void functionCallToSendMailForAccountReactivateRequestRejectedToUser(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            JsonNode jsonNode = parseJson(notificationQueueDto);
            emailTemplates.sendAccountUnblockOrRetrievalFailureMailToUser(
                    getFieldValue(jsonNode, STRING_USERNAME),
                    getFieldValue(jsonNode, STRING_USER_NAME),
                    getFieldValue(jsonNode, STRING_REFERENCE_NUMBER),
                    getFieldValue(jsonNode, STRING_MESSAGE),
                    getFieldValue(jsonNode, STRING_REASON)
            );
        } catch (Exception e) {
            log.error("Notification failed with error: {}", e.getMessage());
            eventPublisherService.publishMessage(FeedbackResponseDto.builder()
                    .requestValueJson(notificationQueueDto)
                    .errorMessage(String.valueOf(e.getMessage()))
                    .build()
            );
        }
    }

    private void functionCallToSendMailForAccountReactivateRequestApprovedToUser(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            JsonNode jsonNode = parseJson(notificationQueueDto);
            emailTemplates.sendAccountUnblockOrRetrievalSuccessfulMailToUser(
                    getFieldValue(jsonNode, STRING_USERNAME),
                    getFieldValue(jsonNode, STRING_USER_NAME),
                    getFieldValue(jsonNode, STRING_REFERENCE_NUMBER),
                    getFieldValue(jsonNode, STRING_MESSAGE)
            );
        } catch (Exception e) {
            log.error("Notification failed with error: {}", e.getMessage());
            eventPublisherService.publishMessage(FeedbackResponseDto.builder()
                    .requestValueJson(notificationQueueDto)
                    .errorMessage(String.valueOf(e.getMessage()))
                    .build()
            );
        }
    }

    private void functionCallToSendMailForNameChangeRequestRejectedToUser(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            JsonNode jsonNode = parseJson(notificationQueueDto);
            emailTemplates.sendNameChangeRequestRejectionMailToUser(
                    getFieldValue(jsonNode, STRING_USERNAME),
                    getFieldValue(jsonNode, STRING_USER_NAME),
                    getFieldValue(jsonNode, STRING_REFERENCE_NUMBER)
            );
        } catch (Exception e) {
            log.error("Notification failed with error: {}", e.getMessage());
            eventPublisherService.publishMessage(FeedbackResponseDto.builder()
                    .requestValueJson(notificationQueueDto)
                    .errorMessage(String.valueOf(e.getMessage()))
                    .build()
            );
        }
    }

    private void functionCallToSendMailForNameChangeRequestApprovedToUser(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            JsonNode jsonNode = parseJson(notificationQueueDto);
            emailTemplates.sendNameChangeRequestApprovedMailToUser(
                    getFieldValue(jsonNode, STRING_USERNAME),
                    getFieldValue(jsonNode, STRING_USER_NAME),
                    getFieldValue(jsonNode, STRING_REFERENCE_NUMBER)
            );
        } catch (Exception e) {
            log.error("Notification failed with error: {}", e.getMessage());
            eventPublisherService.publishMessage(FeedbackResponseDto.builder()
                    .requestValueJson(notificationQueueDto)
                    .errorMessage(String.valueOf(e.getMessage()))
                    .build()
            );
        }
    }

    private void functionCallToSendGmailToAdminThroughContactUsHepDesk(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            JsonNode jsonNode = parseJson(notificationQueueDto);
            emailTemplates.sendContactUsDetailsEmailToAdmin(
                    getFieldValue(jsonNode, STRING_USERNAME),
                    getFieldValue(jsonNode, STRING_USER_NAME),
                    getFieldValue(jsonNode, STRING_PHONE_NUMBER),
                    getFieldValue(jsonNode, STRING_DESCRIPTION)
            );
        } catch (Exception e) {
            log.error("Notification failed with error: {}", e.getMessage());
            eventPublisherService.publishMessage(FeedbackResponseDto.builder()
                    .requestValueJson(notificationQueueDto)
                    .errorMessage(String.valueOf(e.getMessage()))
                    .build()
            );
        }
    }

    private void functionCallToSendGmailSyncCountIncreaseRequestRejectedMailToUser(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            JsonNode jsonNode = parseJson(notificationQueueDto);
            emailTemplates.sendUserGmailSyncCountIncreaseRequestRejection(
                    getFieldValue(jsonNode, STRING_USERNAME),
                    getFieldValue(jsonNode, STRING_USER_NAME)
            );
        } catch (Exception e) {
            log.error("Notification failed with error: {}", e.getMessage());
            eventPublisherService.publishMessage(FeedbackResponseDto.builder()
                    .requestValueJson(notificationQueueDto)
                    .errorMessage(String.valueOf(e.getMessage()))
                    .build()
            );
        }
    }

    private void functionCallToSendGmailSyncCountIncreaseRequestApprovedMailToUser(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            JsonNode jsonNode = parseJson(notificationQueueDto);
            emailTemplates.sendUserGmailSyncApprovedMail(
                    getFieldValue(jsonNode, STRING_USERNAME),
                    getFieldValue(jsonNode, STRING_USER_NAME),
                    Integer.parseInt(getFieldValue(jsonNode, STRING_GMAIL_SYNC_REQUEST_COUNT))
            );
        } catch (Exception e) {
            log.error("Notification failed with error: {}", e.getMessage());
            eventPublisherService.publishMessage(FeedbackResponseDto.builder()
                    .requestValueJson(notificationQueueDto)
                    .errorMessage(String.valueOf(e.getMessage()))
                    .build()
            );
        }
    }

    private void functionCallToSendAdminBlockedUserAlertMailToUser(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            AdminBlockUserDto adminBlockUserDto = StringConstants.objectMapper.readValue(notificationQueueDto.getValueJson(), AdminBlockUserDto.class);
            emailTemplates.sendBlockAlertMailToUser(adminBlockUserDto.getEmail(), adminBlockUserDto.getReason(), adminBlockUserDto.getName(), adminBlockUserDto.getFile());
        } catch (Exception e) {
            log.error("Notification failed with error: {}", e.getMessage());
            eventPublisherService.publishMessage(FeedbackResponseDto.builder()
                    .requestValueJson(notificationQueueDto)
                    .errorMessage(String.valueOf(e.getMessage()))
                    .build()
            );
        }
    }

    private void functionCallToSendSpendingAnalysisMailToUser(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            StatementAnalysisDto statementAnalysisDto = StringConstants.objectMapper.readValue(notificationQueueDto.getValueJson(), StatementAnalysisDto.class);
            emailTemplates.sendSpendingAnalysisEmail(statementAnalysisDto);
        } catch (Exception e) {
            log.error("Notification failed with error: {}", e.getMessage());
            eventPublisherService.publishMessage(FeedbackResponseDto.builder()
                    .requestValueJson(notificationQueueDto)
                    .errorMessage(String.valueOf(e.getMessage()))
                    .build()
            );
        }
    }

    private void functionCallToSendAccountStatementMailToUser(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            StatementAnalysisDto statementAnalysisDto = StringConstants.objectMapper.readValue(notificationQueueDto.getValueJson(), StatementAnalysisDto.class);
            emailTemplates.sendAccountStatementAsEmail(statementAnalysisDto);
        } catch (Exception e) {
            log.error("Notification failed with error: {}", e.getMessage());
            eventPublisherService.publishMessage(FeedbackResponseDto.builder()
                    .requestValueJson(notificationQueueDto)
                    .errorMessage(String.valueOf(e.getMessage()))
                    .build()
            );
        }
    }

    private void functionCallToSendGmailSyncCountIncreaseRequestToAdmin(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            GmailSyncIncreaseRequestDto gmailSyncIncreaseRequestDto = StringConstants.objectMapper.readValue(notificationQueueDto.getValueJson(), GmailSyncIncreaseRequestDto.class);
            emailTemplates.sendUserRaisedGmailSyncRequestEmailToAdmin(gmailSyncIncreaseRequestDto);
        } catch (Exception e) {
            log.error("Notification failed with error: {}", e.getMessage());
            eventPublisherService.publishMessage(FeedbackResponseDto.builder()
                    .requestValueJson(notificationQueueDto)
                    .errorMessage(String.valueOf(e.getMessage()))
                    .build()
            );
        }
    }

    private void functionCallToSendReferenceNumberToUser(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            JsonNode jsonNode = parseJson(notificationQueueDto);
            emailTemplates.sendReferenceNumberEmailToUser(
                    getFieldValue(jsonNode, STRING_USERNAME),
                    getFieldValue(jsonNode, STRING_USER_NAME),
                    getFieldValue(jsonNode, STRING_REFERENCE_NUMBER),
                    getFieldValue(jsonNode, STRING_MESSAGE)
            );
        } catch (Exception e) {
            log.error("Notification failed with error: {}", e.getMessage());
            eventPublisherService.publishMessage(FeedbackResponseDto.builder()
                    .requestValueJson(notificationQueueDto)
                    .errorMessage(String.valueOf(e.getMessage()))
                    .build()
            );
        }
    }

    private void functionCallToSendUserRaisedDefectToAdmin(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            UserRaisedDefectDto userRaisedDefectDto = StringConstants.objectMapper.readValue(notificationQueueDto.getValueJson(), UserRaisedDefectDto.class);
            emailTemplates.sendUserRaiseDefectEmailToAdmin(userRaisedDefectDto);
        } catch (Exception e) {
            log.error("Notification failed with error: {}", e.getMessage());
            eventPublisherService.publishMessage(FeedbackResponseDto.builder()
                    .requestValueJson(notificationQueueDto)
                    .errorMessage(String.valueOf(e.getMessage()))
                    .build()
            );
        }
    }

    private void functionCallForSendingUsernameToUser(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            String username = notificationQueueDto.getValueJson();
            emailTemplates.sendUserNameToUser(username);
        } catch (Exception e) {
            eventPublisherService.publishMessage(FeedbackResponseDto.builder()
                    .requestValueJson(notificationQueueDto)
                    .errorMessage(String.valueOf(e.getMessage()))
                    .build()
            );
        }
    }

    private void functionCallForSendingReportStatusToUser(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            JsonNode jsonNode = parseJson(notificationQueueDto);
            emailTemplates.sendUserReportStatusMailToUser(
                    getFieldValue(jsonNode, STRING_USERNAME),
                    getFieldValue(jsonNode, STRING_USER_NAME),
                    getFieldValue(jsonNode, STRING_REFERENCE_NUMBER),
                    getFieldValue(jsonNode, STRING_MESSAGE)
            );
        } catch (Exception e) {
            log.error("Notification failed with error: {}", e.getMessage());
            eventPublisherService.publishMessage(FeedbackResponseDto.builder()
                    .requestValueJson(notificationQueueDto)
                    .errorMessage(String.valueOf(e.getMessage()))
                    .build()
            );
        }
    }

    private void functionCallForSendingFeedBackMailToAdmin(NotificationQueueDto notificationQueueDto, EmailTemplates emailTemplates) {
        try {
            JsonNode jsonNode = parseJson(notificationQueueDto);
            emailTemplates.sendUserFeedbackEmailToAdmin(
                    getFieldValue(jsonNode, STRING_RATING),
                    getFieldValue(jsonNode, STRING_MESSAGE)
            );
        } catch (Exception e) {
            log.error("Notification failed with error: {}", e.getMessage());
            eventPublisherService.publishMessage(FeedbackResponseDto.builder()
                    .requestValueJson(notificationQueueDto)
                    .errorMessage(String.valueOf(e.getMessage()))
                    .build()
            );
        }
    }

    private JsonNode parseJson(NotificationQueueDto dto) {
        return objectMapper.readTree(dto.getValueJson());
    }

    private static String getFieldValue(JsonNode node, String field) {
        JsonNode jsonNode = node.get(field);
        if (jsonNode == null || jsonNode.isNull()) {
            throw new IllegalArgumentException("Missing field in notification payload: " + field);
        }
        return jsonNode.asString();
    }
}
