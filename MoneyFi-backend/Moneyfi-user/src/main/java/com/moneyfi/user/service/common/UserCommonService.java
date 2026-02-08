package com.moneyfi.user.service.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.moneyfi.user.service.common.dto.request.*;
import com.moneyfi.user.service.common.dto.response.QuoteResponseDto;
import com.moneyfi.user.service.common.dto.response.UserNotificationResponseDto;
import com.moneyfi.user.service.common.dto.response.UserRequestStatusDto;
import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface UserCommonService {

    String uploadUserProfilePictureToS3(String username, Long userId, MultipartFile file) throws IOException;

    ResponseEntity<ByteArrayResource> fetchUserProfilePictureFromS3(String username, Long userId);

    ResponseEntity<ByteArrayResource> getUserRaisedDefectImage(String username, Long defectId);

    ResponseEntity<String> deleteProfilePictureFromS3(String username, Long userId);

    List<UserNotificationResponseDto> getUserNotifications(String username, String status);

    Integer getUserNotificationsCount(String username);

    void updateUserNotificationSeenStatus(String username, String notificationIds);

    List<String> getReasonsForDialogForUser(int reasonCode);

    Map<Boolean, String> sendReferenceRequestNumberEmail(String requestStatus, String email);

    void accountReactivateRequestByUser(AccountRetrieveRequestDto requestDto);

    void nameChangeRequestByUser(NameChangeRequestDto requestDto);

    UserRequestStatusDto trackUserRequestUsingReferenceNumber(String referenceNumber);

    void sendContactUsDetailsToAdmin(HelpCenterContactUsRequestDto requestDto);

    void saveUserNotificationsForParticularUsers(String recipients, Long scheduleId);

    void saveUserNotificationsForAllUsers(List<String> recipients, Long scheduleId);

    ResponseEntity<String> blockOrDeleteAccountByUserRequest(String username, AccountBlockOrDeleteRequestDto request);

    Boolean getUsernameByDetails(ForgotUsernameDto userDetails);

    QuoteResponseDto getTodayQuoteByExternalCall(String externalApiUrl) throws JsonProcessingException;

    void userRequestToIncreaseGmailSyncDailyCount(@Valid GmailSyncCountIncreaseRequestDto request, MultipartFile image, String username) throws JsonProcessingException;
}
