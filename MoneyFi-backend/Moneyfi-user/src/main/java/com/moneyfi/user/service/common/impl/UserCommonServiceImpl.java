package com.moneyfi.user.service.common.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.moneyfi.user.exceptions.ResourceNotFoundException;
import com.moneyfi.user.exceptions.ScenarioNotPossibleException;
import com.moneyfi.user.model.*;
import com.moneyfi.user.model.dto.OtpTempModel;
import com.moneyfi.user.model.dto.UserAuthHist;
import com.moneyfi.user.model.dto.UserAuthModel;
import com.moneyfi.user.model.dto.interfaces.OtpTempProjection;
import com.moneyfi.user.model.dto.interfaces.UserAuthProjection;
import com.moneyfi.user.repository.*;
import com.moneyfi.user.repository.common.CommonServiceRepository;
import com.moneyfi.user.service.common.AwsServices;
import com.moneyfi.user.service.common.CloudinaryService;
import com.moneyfi.user.service.common.CommonService;
import com.moneyfi.user.service.common.UserCommonService;
import com.moneyfi.user.service.common.dto.internal.GmailSyncCountJsonDto;
import com.moneyfi.user.service.common.dto.request.*;
import com.moneyfi.user.service.common.dto.response.QuoteResponseDto;
import com.moneyfi.user.service.common.dto.response.UserNotificationResponseDto;
import com.moneyfi.user.service.common.dto.response.UserRequestStatusDto;
import com.moneyfi.user.util.EmailTemplates;
import com.moneyfi.user.util.constants.StringConstants;
import com.moneyfi.user.util.enums.*;
import com.moneyfi.user.validator.UserValidations;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.moneyfi.user.util.constants.StringConstants.*;

@Service
@Slf4j
public class UserCommonServiceImpl implements UserCommonService {

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    private final CloudinaryService cloudinaryService;
    private final AwsServices awsServices;
    private final ProfileRepository profileRepository;
    private final ContactUsRepository contactUsRepository;
    private final ContactUsHistRepository contactUsHistRepository;
    private final CommonServiceRepository commonServiceRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final EmailTemplates emailTemplates;
    private final ReasonDetailsRepository reasonDetailsRepository;
    private final CommonService commonService;
    private final RestTemplate externalRestTemplate;
    private final ScheduleNotificationRepository scheduleNotificationRepository;

    public UserCommonServiceImpl(CloudinaryService cloudinaryService,
                                 AwsServices awsServices,
                                 ProfileRepository profileRepository,
                                 ContactUsRepository contactUsRepository,
                                 ContactUsHistRepository contactUsHistRepository,
                                 CommonServiceRepository commonServiceRepository,
                                 UserNotificationRepository userNotificationRepository,
                                 EmailTemplates emailTemplates,
                                 ReasonDetailsRepository reasonDetailsRepository,
                                 CommonService commonService,
                                 @Qualifier("externalRestTemplate") RestTemplate externalRestTemplate,
                                 ScheduleNotificationRepository scheduleNotificationRepository){
        this.cloudinaryService = cloudinaryService;
        this.awsServices = awsServices;
        this.profileRepository = profileRepository;
        this.contactUsRepository = contactUsRepository;
        this.contactUsHistRepository = contactUsHistRepository;
        this.commonServiceRepository = commonServiceRepository;
        this.userNotificationRepository = userNotificationRepository;
        this.emailTemplates = emailTemplates;
        this.reasonDetailsRepository = reasonDetailsRepository;
        this.commonService = commonService;
        this.externalRestTemplate = externalRestTemplate;
        this.scheduleNotificationRepository = scheduleNotificationRepository;
    }

    private static final String REFERENCE_NUMBER_SENT = "Reference already sent, Please submit your details";
    private static final String DETAILS_ALREADY_SUBMITTED = "Details are already submitted. Please check the status";
    private static final String USER_IS_NOT_BLOCKED_TODO_THIS = "User is not blocked to perform this operation";

    @Override
    @Transactional(rollbackOn = Exception.class)
    public Map<Boolean, String> sendReferenceRequestNumberEmail(String requestStatus, String email) {
        UserAuthModel user = convertUserAuthInterfaceToDto(profileRepository.getUserDetailsByUsername(email).orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND)));
        Map<Boolean, String> response = new HashMap<>();
        List<ContactUs> contactUsDetails = contactUsRepository.findByEmail(email);
        ProfileModel userProfile = profileRepository.findByUserId(user.getId()).orElseThrow(() -> new ResourceNotFoundException(USER_PROFILE_NOT_FOUND));
        if (requestStatus.equalsIgnoreCase(RequestReason.ACCOUNT_UNBLOCK_REQUEST.name())) {
            if (user.isDeleted() || !user.isBlocked()) {
                throw new ScenarioNotPossibleException(user.isDeleted() ? ACCOUNT_DELETED_MESSAGE : USER_IS_NOT_BLOCKED_TODO_THIS);
            }
            Optional<ContactUs> report = contactUsDetails
                    .stream()
                    .filter(ContactUs::isRequestActive)
                    .filter(i -> i.getRequestReason().equalsIgnoreCase(RequestReason.ACCOUNT_UNBLOCK_REQUEST.name()))
                    .filter(i -> i.getReferenceNumber() != null)
                    .findFirst();
            if (report.isPresent()) {
                throw new ScenarioNotPossibleException(contactUsHistRepository.findByContactUsId(report.get().getId()).size() == 1 ? REFERENCE_NUMBER_SENT :
                        DETAILS_ALREADY_SUBMITTED);
            }
            Optional<ContactUs> requestDetails = contactUsDetails
                    .stream()
                    .filter(ContactUs::isRequestActive)
                    .filter(i -> i.getRequestReason().equalsIgnoreCase(RequestReason.ACCOUNT_BLOCK_REQUEST.name()))
                    .findFirst();
            String referenceNumber = null;
            ContactUs savedRequest = null;
            if (requestDetails.isPresent()) {
                referenceNumber = requestDetails.get().getReferenceNumber();
                requestDetails.get().setRequestStatus(RaiseRequestStatus.INITIATED.name());
                requestDetails.get().setRequestReason(RequestReason.ACCOUNT_UNBLOCK_REQUEST.name());
                savedRequest = contactUsRepository.save(requestDetails.get());
            } else {
                referenceNumber = generateReferenceNumberForUserToSendEmail("BL", userProfile, email.trim());
                ContactUs saveRequest = new ContactUs();
                saveRequest.setEmail(email);
                saveRequest.setReferenceNumber(referenceNumber);
                saveRequest.setRequestActive(true);
                saveRequest.setVerified(false);
                saveRequest.setRequestStatus(RaiseRequestStatus.INITIATED.name());
                saveRequest.setRequestReason(RequestReason.ACCOUNT_UNBLOCK_REQUEST.name());
                saveRequest.setStartTime(CURRENT_DATE_TIME);
                savedRequest = contactUsRepository.save(saveRequest);
            }
            emailTemplates.sendReferenceNumberEmailToUser(userProfile.getName(), email, "account unblock", referenceNumber);
            contactUsHistRepository.save(new ContactUsHist(savedRequest.getId(), null, "Reference number requested to unblock the account", savedRequest.getStartTime(),
                    RequestReason.ACCOUNT_UNBLOCK_REQUEST.name(), RaiseRequestStatus.INITIATED.name()));
            response.put(true, REFERENCE_NUMBER_SENT_MESSAGE);
            return response;
        } else if (requestStatus.equalsIgnoreCase(RequestReason.ACCOUNT_NOT_DELETE_REQUEST.name())) {
            if (!user.isDeleted()) {
                throw new ScenarioNotPossibleException("Account is already active!");
            }
            Optional<ContactUs> report = contactUsDetails
                    .stream()
                    .filter(ContactUs::isRequestActive)
                    .filter(i -> i.getRequestReason().equalsIgnoreCase(RequestReason.ACCOUNT_NOT_DELETE_REQUEST.name()))
                    .filter(i -> i.getReferenceNumber() != null)
                    .findFirst();
            if (report.isPresent()) {
                throw new ScenarioNotPossibleException(contactUsHistRepository.findByContactUsId(report.get().getId()).size() == 1 ? REFERENCE_NUMBER_SENT :
                        DETAILS_ALREADY_SUBMITTED);
            }
            Optional<ContactUs> requestDetails = contactUsDetails
                    .stream()
                    .filter(ContactUs::isRequestActive)
                    .filter(i -> i.getRequestReason().equalsIgnoreCase(RequestReason.ACCOUNT_DELETE_REQUEST.name()))
                    .findFirst();
            String referenceNumber = null;
            ContactUs savedRequest = null;
            if (requestDetails.isPresent()) {
                referenceNumber = requestDetails.get().getReferenceNumber();
                requestDetails.get().setRequestStatus(RaiseRequestStatus.INITIATED.name());
                requestDetails.get().setRequestReason(RequestReason.ACCOUNT_NOT_DELETE_REQUEST.name());
                savedRequest = contactUsRepository.save(requestDetails.get());
            } else {
                referenceNumber = generateReferenceNumberForUserToSendEmail("DL", userProfile, email.trim());
                ContactUs saveRequest = new ContactUs();
                saveRequest.setEmail(email);
                saveRequest.setReferenceNumber(referenceNumber);
                saveRequest.setRequestActive(true);
                saveRequest.setVerified(false);
                saveRequest.setRequestStatus(RaiseRequestStatus.INITIATED.name());
                saveRequest.setRequestReason(RequestReason.ACCOUNT_NOT_DELETE_REQUEST.name());
                saveRequest.setStartTime(CURRENT_DATE_TIME);
                savedRequest = contactUsRepository.save(saveRequest);
            }
            emailTemplates.sendReferenceNumberEmailToUser(userProfile.getName(), email, "account retrieval", referenceNumber);
            contactUsHistRepository.save(new ContactUsHist(savedRequest.getId(), null, "Reference number requested to retrieve the account", savedRequest.getStartTime(),
                    RequestReason.ACCOUNT_NOT_DELETE_REQUEST.name(), RaiseRequestStatus.INITIATED.name()));
            response.put(true, REFERENCE_NUMBER_SENT_MESSAGE);
            return response;
        } else if (requestStatus.equalsIgnoreCase(RequestReason.NAME_CHANGE_REQUEST.name())) {
            if (user.isDeleted() || user.isBlocked()) {
                throw new ScenarioNotPossibleException(user.isDeleted() ? ACCOUNT_DELETED_MESSAGE :
                        "Name change is not possible since user is blocked");
            }
            Optional<ContactUs> report = contactUsDetails
                    .stream()
                    .filter(ContactUs::isRequestActive)
                    .filter(i -> i.getRequestReason().equalsIgnoreCase(RequestReason.NAME_CHANGE_REQUEST.name()))
                    .filter(i -> i.getReferenceNumber() != null)
                    .findFirst();
            if (report.isPresent()) {
                throw new ScenarioNotPossibleException(contactUsHistRepository.findByContactUsId(report.get().getId()).size() == 1 ? REFERENCE_NUMBER_SENT :
                        DETAILS_ALREADY_SUBMITTED);
            }
            String referenceNumber = generateReferenceNumberForUserToSendEmail("NA", userProfile, email.trim());
            emailTemplates.sendReferenceNumberEmailToUser(userProfile.getName(), email, "change name", referenceNumber);
            ContactUs saveRequest = new ContactUs();
            saveRequest.setEmail(email);
            saveRequest.setReferenceNumber(referenceNumber);
            saveRequest.setRequestActive(true);
            saveRequest.setVerified(false);
            saveRequest.setRequestStatus(RaiseRequestStatus.INITIATED.name());
            saveRequest.setRequestReason(RequestReason.NAME_CHANGE_REQUEST.name());
            saveRequest.setStartTime(CURRENT_DATE_TIME);
            ContactUs savedRequest = contactUsRepository.save(saveRequest);

            ContactUsHist userRequestHist = new ContactUsHist();
            userRequestHist.setContactUsId(savedRequest.getId());
            userRequestHist.setRequestStatus(RaiseRequestStatus.INITIATED.name());
            userRequestHist.setRequestReason(RequestReason.NAME_CHANGE_REQUEST.name());
            userRequestHist.setMessage("Request Reference to change the name");
            userRequestHist.setUpdatedTime(savedRequest.getStartTime());
            contactUsHistRepository.save(userRequestHist);
            response.put(true, REFERENCE_NUMBER_SENT_MESSAGE);
            return response;
        }
        response.put(false, INVALID_REQUEST_MESSAGE);
        return response;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void accountReactivateRequestByUser(AccountRetrieveRequestDto requestDto) {
        String requestReason;
        ContactUsHist userRequestHist = new ContactUsHist();
        if (requestDto.getRequestReason().equalsIgnoreCase(RequestReason.ACCOUNT_UNBLOCK_REQUEST.name())) {
            requestReason = RequestReason.ACCOUNT_UNBLOCK_REQUEST.name();
            userRequestHist.setRequestReason(requestReason);
        } else if (requestDto.getRequestReason().equalsIgnoreCase(RequestReason.ACCOUNT_NOT_DELETE_REQUEST.name())) {
            requestReason = RequestReason.ACCOUNT_NOT_DELETE_REQUEST.name();
            userRequestHist.setRequestReason(requestReason);
        } else {
            requestReason = null;
        }
        Optional<ContactUs> report = contactUsRepository.findByEmail(requestDto.getUsername())
                .stream()
                .filter(ContactUs::isRequestActive)
                .filter(i -> i.getRequestReason().equalsIgnoreCase(requestReason))
                .findFirst();
        ContactUs user = report.orElseThrow(() -> new ResourceNotFoundException("User request not found"));
        if (user.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.INITIATED.name())) {
            if (!user.getReferenceNumber().equals(requestDto.getReferenceNumber()))
                throw new ScenarioNotPossibleException(INCORRECT_REFERENCE_NUMBER);
            user.setRequestStatus(RaiseRequestStatus.SUBMITTED.name());
            ContactUs savedRequest = contactUsRepository.save(user);

            userRequestHist.setContactUsId(savedRequest.getId());
            userRequestHist.setName(requestDto.getName());
            userRequestHist.setMessage(requestDto.getDescription());
            userRequestHist.setUpdatedTime(CURRENT_DATE_TIME);
            userRequestHist.setRequestStatus(RaiseRequestStatus.SUBMITTED.name());
            contactUsHistRepository.save(userRequestHist);
        } else if (user.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.SUBMITTED.name())) {
            throw new ScenarioNotPossibleException("Request already raised");
        }
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void nameChangeRequestByUser(NameChangeRequestDto requestDto) {
        if (requestDto.getDescription() == null || requestDto.getDescription().trim().isEmpty()) {
            throw new ScenarioNotPossibleException("Description can't be empty");
        }
        List<ContactUs> contactUsDetails = contactUsRepository.findByEmail(requestDto.getEmail());
        Optional<ContactUs> report = contactUsDetails
                .stream()
                .filter(ContactUs::isRequestActive)
                .filter(i -> i.getRequestReason().equalsIgnoreCase(RequestReason.NAME_CHANGE_REQUEST.name()))
                .findFirst();
        ContactUs user = report.orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
        if (!user.getReferenceNumber().equals(requestDto.getReferenceNumber()))
            throw new ScenarioNotPossibleException(INCORRECT_REFERENCE_NUMBER);
        user.setRequestStatus(RaiseRequestStatus.SUBMITTED.name());
        ContactUs savedRequest = contactUsRepository.save(user);

        ContactUsHist userRequestHist = new ContactUsHist();
        userRequestHist.setContactUsId(savedRequest.getId());
        userRequestHist.setName(requestDto.getNewName());
        userRequestHist.setMessage(requestDto.getOldName() + "," + requestDto.getDescription());
        userRequestHist.setUpdatedTime(CURRENT_DATE_TIME);
        userRequestHist.setRequestReason(RequestReason.NAME_CHANGE_REQUEST.name());
        userRequestHist.setRequestStatus(RaiseRequestStatus.SUBMITTED.name());
        contactUsHistRepository.save(userRequestHist);
    }

    @Override
    public UserRequestStatusDto trackUserRequestUsingReferenceNumber(String referenceNumber) {
        UserRequestStatusDto userRequestResponse = commonServiceRepository.trackUserRequestUsingReferenceNumber(referenceNumber);

        if (userRequestResponse.getRequestType().equalsIgnoreCase(RequestReason.NAME_CHANGE_REQUEST.name())) {
            userRequestResponse.setRequestType("Requested for Name change");
        } else if (userRequestResponse.getRequestType().equalsIgnoreCase(RequestReason.ACCOUNT_UNBLOCK_REQUEST.name())) {
            userRequestResponse.setRequestType("Requested to unblock the account");
        } else if (userRequestResponse.getRequestType().equalsIgnoreCase(RequestReason.ACCOUNT_NOT_DELETE_REQUEST.name())) {
            userRequestResponse.setRequestType("Requested to retrieve the account");
        } else if (userRequestResponse.getRequestType().equalsIgnoreCase(RequestReason.USER_DEFECT_UPDATE.name())) {
            userRequestResponse.setRequestType("Issue raised");
        } else if (userRequestResponse.getRequestType().equalsIgnoreCase(RequestReason.ACCOUNT_BLOCK_REQUEST.name())) {
            userRequestResponse.setRequestType("Request Raised to block account");
        }

        if (userRequestResponse.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.INITIATED.name())) {
            userRequestResponse.setRequestStatus("User yet to submit the details - open");
        } else if (userRequestResponse.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.SUBMITTED.name())) {
            if (userRequestResponse.getRequestType().equalsIgnoreCase("Request Raised to block account"))
                userRequestResponse.setRequestStatus("Account blocked, Raise request to unblock");
            else userRequestResponse.setRequestStatus("Admin approval is in progress");
        } else if (userRequestResponse.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.COMPLETED.name())) {
            userRequestResponse.setRequestStatus("Request has been approved by admin");
        }
        return userRequestResponse;
    }

    @Override
    public void sendContactUsDetailsToAdmin(HelpCenterContactUsRequestDto requestDto) {
        if(requestDto.getEmail() == null || requestDto.getPhoneNumber() == null || requestDto.getName() == null || requestDto.getDescription() == null){
            throw new ScenarioNotPossibleException("Input fields cannot be null");
        }
        String email = requestDto.getEmail().trim();
        String phoneNumber = requestDto.getPhoneNumber().trim();
        if(email.isEmpty() || phoneNumber.isEmpty() || requestDto.getName().trim().isEmpty() || requestDto.getDescription().trim().isEmpty()) {
            throw new ScenarioNotPossibleException("Please fill all the fields");
        }
        if (!phoneNumber.matches("^[0-9]{10}$")) {
            throw new ScenarioNotPossibleException("Please enter a valid 10-digit phone number");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new ScenarioNotPossibleException("Please enter a valid email address");
        }
        emailTemplates.sendContactUsDetailsEmailToAdmin(email, requestDto.getPhoneNumber(), requestDto.getName(), requestDto.getDescription());
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void saveUserNotificationsForParticularUsers(String recipients, Long scheduleId) {
        List<UserNotification> userNotificationListForSpecifiedUsers = new ArrayList<>();
        Arrays.stream(recipients.split(","))
                .map(String::trim)
                .forEach(username -> userNotificationListForSpecifiedUsers.add(new UserNotification(username, scheduleId, false)));
        userNotificationRepository.saveAll(userNotificationListForSpecifiedUsers);
        commonService.asyncNotificationHandler(userNotificationListForSpecifiedUsers, scheduleId);
    }

    @Override
    @Async
    public void saveUserNotificationsForAllUsers(List<String> users, Long scheduleId) {
        List<UserNotification> fullBatchList = new ArrayList<>();
        List<UserNotification> batch = new ArrayList<>();
        for (String username : users) {
            batch.add(new UserNotification(username, scheduleId, false));
            if (batch.size() == 1000) {
                fullBatchList.addAll(batch);
                userNotificationRepository.saveAll(batch);
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            fullBatchList.addAll(batch);
            userNotificationRepository.saveAll(batch);
        }
        commonService.asyncNotificationHandler(fullBatchList, scheduleId);
    }

    @Override
    public String uploadUserProfilePictureToS3(String username, Long userId, MultipartFile file) {
        if (LOCAL_PROFILE.equalsIgnoreCase(activeProfile)) {
            cloudinaryService.uploadPictureToCloudinary(file, userId, username, UPLOAD_PROFILE_PICTURE);
            return "Upload Successful";
        } else {
            return awsServices.uploadPictureToS3(userId, username, file, UPLOAD_PROFILE_PICTURE);
        }
    }

    @Override
    public ResponseEntity<ByteArrayResource> fetchUserProfilePictureFromS3(String username, Long userId) {
        if (LOCAL_PROFILE.equalsIgnoreCase(activeProfile)) {
            byte[] imageBytes = cloudinaryService.getImageFromCloudinary(userId, username, UPLOAD_PROFILE_PICTURE);
            ByteArrayResource resource = new ByteArrayResource(imageBytes);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .contentLength(imageBytes.length)
                    .body(resource);
        } else {
            return awsServices.fetchUserProfilePictureFromS3(userId, username);
        }
    }

    @Override
    public ResponseEntity<ByteArrayResource> getUserRaisedDefectImage(String username, Long defectId) {
        if (LOCAL_PROFILE.equalsIgnoreCase(activeProfile)) {
            byte[] imageBytes = cloudinaryService.getImageFromCloudinary(defectId, username, UPLOAD_USER_RAISED_REPORT_PICTURE);
            ByteArrayResource resource = new ByteArrayResource(imageBytes);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .contentLength(imageBytes.length)
                    .body(resource);
        } else {
            return awsServices.fetchUserProfilePictureFromS3(defectId, username);
        }
    }

    @Override
    public ResponseEntity<String> deleteProfilePictureFromS3(String username, Long userId) {
        if (LOCAL_PROFILE.equalsIgnoreCase(activeProfile)) {
            return cloudinaryService.deleteProfilePictureFromCloudinary(userId, username);
        } else {
            return awsServices.deleteProfilePictureFromS3(userId, username);
        }
    }

    @Override
    public List<UserNotificationResponseDto> getUserNotifications(String username, String status) {
        return commonServiceRepository.getUserNotifications(username, status).stream()
                .peek(notification -> {
                    if (notification.getNotificationType().equalsIgnoreCase(UserRequestType.GMAIL_SYNC_COUNT_INCREASE.name())) {
                        GmailSyncCountJsonDto gmailSyncCountJsonDto = null;
                        try {
                            gmailSyncCountJsonDto = objectMapper.readValue(notification.getDescription(), GmailSyncCountJsonDto.class);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                        if (notification.getRole().equalsIgnoreCase(UserRoles.USER.name())) {
                            notification.setDescription("Requested count: " + gmailSyncCountJsonDto.getCount() + " | " + "Requested Reason: " + gmailSyncCountJsonDto.getReason());
                        } else if (notification.getRole().equalsIgnoreCase(UserRoles.ADMIN.name())) {
                            GmailSyncCountJsonDto parentGmailSyncCountJsonDto = null;
                            try {
                                parentGmailSyncCountJsonDto = objectMapper.readValue(scheduleNotificationRepository.findById(notification.getParentKey()).get().getDescription(), GmailSyncCountJsonDto.class);
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                            notification.setDescription("Requested count: " + parentGmailSyncCountJsonDto.getCount() + " | " + "Approved Count: " + gmailSyncCountJsonDto.getCount() + " | " + "Remarks: " + gmailSyncCountJsonDto.getReason());
                        }
                    }
                }).toList();
    }

    @Override
    public Integer getUserNotificationsCount(String username) {
        return Math.toIntExact(getUserNotifications(username, "ACTIVE")
                .stream()
                .filter(notification -> !notification.isRead())
                .count());
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void updateUserNotificationSeenStatus(String username, String notificationIds) {
        List<UserNotification> userNotificationListToUpdate = new ArrayList<>();
        Arrays.stream(notificationIds.split(","))
                .map(String::trim)
                .map(Long::parseLong)
                .toList()
                .forEach(notificationId -> {
                    Optional<UserNotification> notification = userNotificationRepository.findByScheduleIdAndUsername(notificationId, username);
                    if(notification.isPresent()){
                        notification.get().setRead(true);
                        userNotificationListToUpdate.add(notification.get());
                    }
                });
        userNotificationRepository.saveAll(userNotificationListToUpdate);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<String> getReasonsForDialogForUser(int reasonCode) {
        return reasonDetailsRepository.findAll()
                .stream()
                .filter(reasonDetails -> reasonDetails.getReasonCode() == reasonCode && !reasonDetails.getIsDeleted())
                .map(ReasonDetails::getReason)
                .toList();
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public ResponseEntity<String> blockOrDeleteAccountByUserRequest(String username, AccountBlockOrDeleteRequestDto request) {
        UserAuthModel user = convertUserAuthInterfaceToDto(profileRepository.getUserDetailsByUsername(username.trim()).orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND)));
        UserValidations.userAccountDeactivationInputValidation(request);
        UserValidations.userAlreadyDeactivatedCheckValidation(user);

        String deactivationType;
        String referencePrefix;
        if (request.getDeactivationType().equalsIgnoreCase(AccDeactivationType.BLOCK.name())) {
            deactivationType = OtpType.ACCOUNT_BLOCK.name();
            referencePrefix = "BL";
        } else if (request.getDeactivationType().equalsIgnoreCase(AccDeactivationType.DELETE.name())) {
            deactivationType = OtpType.ACCOUNT_DELETE.name();
            referencePrefix = "DL";
        } else {
            throw new ScenarioNotPossibleException(INVALID_REQUEST_MESSAGE);
        }

        Optional<OtpTempProjection> otpTempProjection = profileRepository.getOtpTempDetails(username, deactivationType, CURRENT_DATE_TIME);
        if(otpTempProjection.isPresent()){
            OtpTempModel response = StringConstants.convertOtpTempModelInterfaceToDto(otpTempProjection.get());
            UserValidations.otpCheckDuringAccountDeactivationValidations(request, response, user);

            ProfileModel userProfile = profileRepository.findByUserId(user.getId()).orElseThrow(() -> new ResourceNotFoundException(USER_PROFILE_NOT_FOUND));
            String referenceNumber = StringConstants.generateReferenceNumberForUserToSendEmail(referencePrefix, userProfile, username);

            ContactUs accountDeactivationRequest = new ContactUs();
            UserAuthHist userAuthHist = new UserAuthHist();
            ContactUsHist blockAccountOrDeleteRequestHistory = new ContactUsHist();
            if (deactivationType.equalsIgnoreCase(OtpType.ACCOUNT_BLOCK.name())) {
                profileRepository.updateUserAuthTableWithBlockOrUnblockStatus(user.getId(), true);
                accountDeactivationRequest.setRequestReason(RequestReason.ACCOUNT_BLOCK_REQUEST.name());
                userAuthHist.setReasonTypeId(reasonCodeIdAssociation.get(ReasonEnum.BLOCK_ACCOUNT));
                blockAccountOrDeleteRequestHistory.setMessage(BLOCKED_BY_USER + ", " + request.getDescription());
            } else {
                profileRepository.updateUserAuthTableWithDeleteOrUndeleteStatus(user.getId(), true);
                accountDeactivationRequest.setRequestReason(RequestReason.ACCOUNT_DELETE_REQUEST.name());
                userAuthHist.setReasonTypeId(reasonCodeIdAssociation.get(ReasonEnum.DELETE_ACCOUNT));
                blockAccountOrDeleteRequestHistory.setMessage("Deleted by User," + request.getDescription());
            }
            accountDeactivationRequest.setEmail(username);
            accountDeactivationRequest.setReferenceNumber(referenceNumber);
            accountDeactivationRequest.setRequestActive(true);
            accountDeactivationRequest.setVerified(false);
            accountDeactivationRequest.setRequestStatus(RaiseRequestStatus.SUBMITTED.name());
            accountDeactivationRequest.setStartTime(CURRENT_DATE_TIME);
            ContactUs savedRequest = contactUsRepository.save(accountDeactivationRequest);

            blockAccountOrDeleteRequestHistory.setName(userProfile.getName());
            blockAccountOrDeleteRequestHistory.setContactUsId(savedRequest.getId());
            blockAccountOrDeleteRequestHistory.setUpdatedTime(savedRequest.getStartTime());
            blockAccountOrDeleteRequestHistory.setRequestReason(savedRequest.getRequestReason());
            blockAccountOrDeleteRequestHistory.setRequestStatus(savedRequest.getRequestStatus());
            contactUsHistRepository.save(blockAccountOrDeleteRequestHistory);

            userAuthHist.setUserId(user.getId());
            userAuthHist.setComment(request.getDescription());
            userAuthHist.setUpdatedBy(user.getId());
            userAuthHist.setUpdatedTime(savedRequest.getStartTime());
            profileRepository.insertUserAuthHistory(userAuthHist.getUserId(), userAuthHist.getUpdatedTime(), userAuthHist.getReasonTypeId(), userAuthHist.getComment(), userAuthHist.getUpdatedBy());
            new Thread(
                    () -> {
                        int rowsAffected = profileRepository.deleteByEmailAndRequestType(username, deactivationType);
                        log.info("Number of OTP entries deleted: {}", rowsAffected);
                        /** emailTemplates.sendReferenceNumberEmail(userProfile.getName(), username, "account block", referenceNumber); **/
                    }
            ).start();
            return ResponseEntity.ok("Account " + (request.getDeactivationType().equalsIgnoreCase(AccDeactivationType.BLOCK.name()) ? "Blocked" : "Deleted") + " successfully");
        } else {
            throw new ResourceNotFoundException("Otp request not found");
        }
    }

    @Override
    public Boolean getUsernameByDetails(ForgotUsernameDto userDetails) {
        String username = functionCallToRetrieveUsername(userDetails);
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        log.info("Username fetched: {}", username);
        emailTemplates.sendUserNameToUser(username);
        return true;
    }

    @Override
    public QuoteResponseDto getTodayQuoteByExternalCall(String externalApiUrl) {
        QuoteResponseDto quoteResponseDto = new QuoteResponseDto();
        try {
            String jsonStringResponse = externalRestTemplate.getForObject(DAILY_QUOTE_EXTERNAL_API_URL, String.class);

            List<QuoteResponseDto> quoteList = StringConstants.objectMapper.readValue(jsonStringResponse, new TypeReference<List<QuoteResponseDto>>() {});
            if(!quoteList.isEmpty()){
                quoteResponseDto.setQuote(quoteList.get(0).getQuote());
                quoteResponseDto.setAuthor(quoteList.get(0).getAuthor());
                quoteResponseDto.setDescription(quoteList.get(0).getDescription());
                return quoteResponseDto;
            }
        } catch (Exception e){
            e.printStackTrace();
            throw new ScenarioNotPossibleException("Failed to parse the json response -> " + e);
        }
        throw new ResourceNotFoundException("No quote response found from external api");
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void userRequestToIncreaseGmailSyncDailyCount(GmailSyncCountIncreaseRequestDto request, MultipartFile image, String username) throws JsonProcessingException {
        UserValidations.validateUserGmailSyncCountIncreaseRequest(request);
        ContactUs existingRequest = contactUsRepository.findByEmail(username).stream()
                .filter(gmailSyncRequest -> gmailSyncRequest.getRequestReason().equalsIgnoreCase(RequestReason.GMAIL_SYNC_REQUEST_TYPE.name()) && gmailSyncRequest.getStartTime().toLocalDate().equals(LocalDate.now()))
                .findFirst().orElse(null);
        if(ObjectUtils.isNotEmpty(existingRequest)) {
            throw new ScenarioNotPossibleException("Request already " + existingRequest.getRequestStatus().substring(0,1).toUpperCase() + existingRequest.getRequestStatus().substring(1).toLowerCase());
        }

        UserAuthModel user = convertUserAuthInterfaceToDto(profileRepository.getUserDetailsByUsername(username).orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND)));
        ProfileModel userProfile = profileRepository.findByUserId(user.getId()).orElseThrow(() -> new ResourceNotFoundException(USER_PROFILE_NOT_FOUND));
        String referenceNumber = StringConstants.generateReferenceNumberForUserToSendEmail("GS", userProfile, username);

        ContactUs contactUs = new ContactUs();
        contactUs.setEmail(username);
        contactUs.setReferenceNumber(referenceNumber);
        contactUs.setRequestActive(Boolean.TRUE);
        contactUs.setRequestReason(RequestReason.GMAIL_SYNC_REQUEST_TYPE.name());
        contactUs.setVerified(Boolean.FALSE);
        contactUs.setRequestStatus(RaiseRequestStatus.SUBMITTED.name());
        contactUs.setStartTime(CURRENT_DATE_TIME);
        ContactUs savedRequest = contactUsRepository.save(contactUs);
        contactUsHistRepository.save(new ContactUsHist(savedRequest.getId(), userProfile.getName(), request.getReason(), savedRequest.getStartTime(), savedRequest.getRequestReason(), savedRequest.getRequestStatus()));

        GmailSyncCountJsonDto gmailSyncCountJsonDto = new GmailSyncCountJsonDto(request.getCount(), request.getReason(), savedRequest.getId());
        String jsonString = StringConstants.objectMapper.writeValueAsString(gmailSyncCountJsonDto);

        ScheduleNotification scheduleNotification = new ScheduleNotification();
        scheduleNotification.setSubject("Gmail Sync Request Count Increase");
        scheduleNotification.setDescription(jsonString);
        scheduleNotification.setScheduleFrom(CURRENT_DATE_TIME);
        scheduleNotification.setScheduleTo(LocalDate.now().atTime(LocalTime.MAX));
        scheduleNotification.setRecipients(username);
        scheduleNotification.setScheduleBy(user.getId());
        scheduleNotification.setUpdatedBy(user.getId());
        scheduleNotification.setNotificationType(UserRequestType.GMAIL_SYNC_COUNT_INCREASE.name());
        saveUserNotificationsForParticularUsers(username, scheduleNotificationRepository.save(scheduleNotification).getId());

        if (ObjectUtils.isNotEmpty(image)) {
            if (LOCAL_PROFILE.equalsIgnoreCase(activeProfile)) {
                cloudinaryService.uploadPictureToCloudinary(image, savedRequest.getId(), username, GMAIL_SYNC_COUNT_INCREASE_REQUEST);
            } else {
                awsServices.uploadPictureToS3(savedRequest.getId(), username, image, GMAIL_SYNC_COUNT_INCREASE_REQUEST);
            }
        }
        new Thread(() -> {
            emailTemplates.sendUserRaisedGmailSyncRequestEmailToAdmin(request, userProfile.getName(), username, image != null && !image.isEmpty() ? image : null);
            emailTemplates.sendReferenceNumberEmailToUser(userProfile.getName(), username, "request Gmail Sync Count Increase", referenceNumber);
        }).start();
    }

    private String functionCallToRetrieveUsername(ForgotUsernameDto userDetails) {
        String username = "";

        if (userDetails.getPhoneNumber() != null && userDetails.getPhoneNumber().length() == 10) {
            List<ProfileModel> fetchedUsers = profileRepository.findByPhone(userDetails.getPhoneNumber().trim());
            if (fetchedUsers.size() == 1) {
                return functionToGetUsernameUsingUserId(fetchedUsers.get(0).getUserId());
            }

            fetchedUsers = fetchedUsers
                    .stream()
                    .filter(user -> user.getDateOfBirth().equals(userDetails.getDateOfBirth()))
                    .toList();
            if (fetchedUsers.size() == 1) {
                return functionToGetUsernameUsingUserId(fetchedUsers.get(0).getUserId());
            }

            fetchedUsers = fetchedUsers
                    .stream()
                    .filter(user -> user.getName().equalsIgnoreCase(userDetails.getName()))
                    .toList();
            if (fetchedUsers.size() == 1) {
                return functionToGetUsernameUsingUserId(fetchedUsers.get(0).getUserId());
            }

            fetchedUsers = fetchedUsers
                    .stream()
                    .filter(user -> user.getGender().equalsIgnoreCase(userDetails.getGender())
                            && user.getMaritalStatus().equalsIgnoreCase(userDetails.getMaritalStatus()))
                    .toList();
            if (fetchedUsers.size() == 1) {
                return functionToGetUsernameUsingUserId(fetchedUsers.get(0).getUserId());
            }

            List<String> matchedUsernames = functionToFetchUserByPinCode(fetchedUsers, userDetails);
            if (matchedUsernames.size() == 1) {
                return matchedUsernames.get(0);
            }
            username += "null";
        }
        return functionCallToFetchUsernameByUserDetailsWithoutPhoneNumber(username, userDetails);
    }

    private List<String> functionToFetchUserByPinCode(List<ProfileModel> fetchedUsers, ForgotUsernameDto userDetails) {
        List<String> matchedUsernames = new ArrayList<>();
        for (ProfileModel profile : fetchedUsers) {
            String address = profile.getAddress();
            if (address != null && !address.isEmpty()) {
                Pattern pattern = Pattern.compile("\\b\\d{6}\\b");
                Matcher matcher = pattern.matcher(address);
                String pincode = null;
                if (matcher.find()) {
                    pincode = matcher.group();
                    if (pincode.equals(userDetails.getPinCode())) {
                        matchedUsernames.add(functionToGetUsernameUsingUserId(fetchedUsers.get(0).getUserId()));
                    }
                }
            }
        }
        return matchedUsernames;
    }

    private String functionToGetUsernameUsingUserId(Long userId) {
        Optional<UserAuthProjection> userAuthProjection = profileRepository.getUserAuthModelByUserId(userId);
        if (userAuthProjection.isPresent()) {
            return convertUserAuthInterfaceToDto(userAuthProjection.get()).getUsername();
        } else {
            throw new ResourceNotFoundException(USER_NOT_FOUND);
        }
    }

    private String functionCallToFetchUsernameByUserDetailsWithoutPhoneNumber(String username, ForgotUsernameDto userDetails) {
        if (username.isEmpty() || username.equalsIgnoreCase("null")) {
            List<ProfileModel> fetchedUsersByAllDetails =
                    profileRepository.findByUserProfileDetails(userDetails.getDateOfBirth(), userDetails.getName(), userDetails.getGender(), userDetails.getMaritalStatus());
            if (fetchedUsersByAllDetails.size() == 1) {
                return functionToGetUsernameUsingUserId(fetchedUsersByAllDetails.get(0).getUserId());
            }
            List<String> matchedUsernames = functionToFetchUserByPinCode(fetchedUsersByAllDetails, userDetails);
            if (matchedUsernames.size() == 1) {
                return matchedUsernames.get(0);
            }
        }
        return null;
    }
}
