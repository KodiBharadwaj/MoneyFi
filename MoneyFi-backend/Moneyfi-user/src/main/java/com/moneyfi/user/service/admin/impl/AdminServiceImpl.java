package com.moneyfi.user.service.admin.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.moneyfi.user.exceptions.FileUploadException;
import com.moneyfi.user.exceptions.ResourceNotFoundException;
import com.moneyfi.user.exceptions.ScenarioNotPossibleException;
import com.moneyfi.user.model.*;
import com.moneyfi.user.model.dto.UserAuthHist;
import com.moneyfi.user.model.dto.UserAuthModel;
import com.moneyfi.user.model.dto.interfaces.ExcelTemplateListProjection;
import com.moneyfi.user.model.dto.interfaces.UserAuthHistProjection;
import com.moneyfi.user.repository.*;
import com.moneyfi.user.repository.admin.AdminRepository;
import com.moneyfi.user.repository.common.CommonServiceRepository;
import com.moneyfi.user.service.admin.AdminService;
import com.moneyfi.user.service.admin.dto.request.AdminScheduleRequestDto;
import com.moneyfi.user.service.admin.dto.request.ReasonDetailsRequestDto;
import com.moneyfi.user.service.admin.dto.request.ReasonUpdateRequestDto;
import com.moneyfi.user.service.admin.dto.request.ScheduleNotificationRequestDto;
import com.moneyfi.user.service.admin.dto.response.*;
import com.moneyfi.user.service.common.AwsServices;
import com.moneyfi.user.service.common.CloudinaryService;
import com.moneyfi.user.service.common.UserCommonService;
import com.moneyfi.user.service.common.dto.emaildto.AdminBlockUserDto;
import com.moneyfi.user.service.common.dto.internal.GmailSyncCountJsonDto;
import com.moneyfi.user.service.common.dto.internal.NotificationQueueDto;
import com.moneyfi.user.service.common.dto.response.UserFeedbackResponseDto;
import com.moneyfi.user.util.constants.StringConstants;
import com.moneyfi.user.util.enums.*;
import com.moneyfi.user.validator.AdminValidations;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.moneyfi.user.util.constants.StringConstants.*;
import static com.moneyfi.user.util.constants.StringConstants.USER_NOT_FOUND;
import static com.moneyfi.user.util.enums.SchedulingNotificationType.ADMIN_SCHEDULING;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    private static final String STATUS_REJECTED = "REJECTED";
    private static final String STATUS_APPROVED = "APPROVED";

    private final AdminRepository adminRepository;
    private final ContactUsRepository contactUsRepository;
    private final ProfileRepository profileRepository;
    private final ContactUsHistRepository contactUsHistRepository;
    private final ReasonDetailsRepository reasonDetailsRepository;
    private final CommonServiceRepository commonServiceRepository;
    private final ScheduleNotificationRepository scheduleNotificationRepository;
    private final UserCommonService userCommonService;
    private final UserNotificationRepository userNotificationRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ExcelTemplateRepository excelTemplateRepository;
    private final CloudinaryService cloudinaryService;
    private final AwsServices awsServices;

    @Override
    public AdminOverviewPageDto getAdminOverviewPageDetails() {
        AdminOverviewPageDto overviewPageDetails = adminRepository.getAdminOverviewPageDetails();
        overviewPageDetails.setTotalUsers(overviewPageDetails.getActiveUsers() + overviewPageDetails.getBlockedUsers() + overviewPageDetails.getDeletedUsers());
        return overviewPageDetails;
    }

    @Override
    public List<UserGridDto> getUserDetailsGridForAdmin(String status) {
        AtomicInteger i = new AtomicInteger(1);
        return adminRepository.getUserDetailsGridForAdmin(status)
                .stream()
                .peek(user -> user.setSlNo(i.getAndIncrement())).toList();
    }

    @Override
    public byte[] getUserDetailsExcelForAdmin(String status) {
        List<UserGridDto> userGridDtoList = getUserDetailsGridForAdmin(status);
        if(userGridDtoList.isEmpty()){
            throw new ResourceNotFoundException("No user data found to generate excel");
        }
        return generateExcelReport(userGridDtoList);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public boolean accountReactivationAndNameChangeRequest(String email, String referenceNumber, String requestStatus, Long adminUserId, String approveStatus, String declineReason, int gmailSyncRequestCount) {
        return contactUsRepository.findByEmail(email)
                .stream()
                .filter(ContactUs::isRequestActive)
                .filter(i -> i.getReferenceNumber() != null &&
                        i.getReferenceNumber().trim().equalsIgnoreCase(referenceNumber.trim()))
                .findFirst()
                .map(request -> {
                    UserAuthModel user = convertUserAuthInterfaceToDto(profileRepository.getUserDetailsByUsername(email).orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND)));
                    ProfileModel userProfile = profileRepository.findByUserId(user.getId()).orElseThrow(() -> new ResourceNotFoundException(USER_PROFILE_NOT_FOUND));
                    if (approveStatus.equalsIgnoreCase(ApproveStatus.APPROVE.name())) {
                        try {
                            functionCallToChangeDetails(user, userProfile, email, request, requestStatus, adminUserId, gmailSyncRequestCount);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    } else if (approveStatus.equalsIgnoreCase(ApproveStatus.DECLINE.name())) {
                        try {
                            functionCallToDeclineTheUserRequest(user, userProfile, request, declineReason, email, adminUserId, gmailSyncRequestCount);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return true;
                })
                .orElse(false);
    }

    @Override
    public List<UserRequestsGridDto> getUserRequestsGridForAdmin(String status) {
        String requestReason = null;
        if(status.equalsIgnoreCase("Rename")){
            requestReason = RequestReason.NAME_CHANGE_REQUEST.name();
        } else if(status.equalsIgnoreCase("Unblock")){
            requestReason = RequestReason.ACCOUNT_UNBLOCK_REQUEST.name();
        } else if(status.equalsIgnoreCase("Retrieve")){
            requestReason = RequestReason.ACCOUNT_NOT_DELETE_REQUEST.name();
        } else if(status.equalsIgnoreCase("Other")){
            requestReason = RequestReason.GMAIL_SYNC_REQUEST_TYPE.name();
        } else {
            requestReason = ALL;
        }
        List<UserRequestsGridDto> userRequestsGridDtoList = adminRepository.getUserRequestsGridForAdmin(requestReason);
        userRequestsGridDtoList.forEach(userGrid -> {
            if((status.equalsIgnoreCase("Rename") || status.equalsIgnoreCase(ALL))
                    && userGrid.getRequestType().equalsIgnoreCase("Name Change")){
                userGrid.setDescription("My old name: " + userGrid.getDescription());
                userGrid.setName("New Name: " + userGrid.getName());
            }
            if(userGrid.getRequestType().equalsIgnoreCase("Gmail Sync Count Increase Request")) {
                GmailSyncCountJsonDto gmailSyncCountJsonDto = null;
                try {
                    gmailSyncCountJsonDto = objectMapper.readValue(userGrid.getDescription(), GmailSyncCountJsonDto.class);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                userGrid.setDescription("Requested Count: " + gmailSyncCountJsonDto.getCount() + " | " + "Reason: " + gmailSyncCountJsonDto.getReason());
            }
        });
        return userRequestsGridDtoList;
    }

    @Override
    public List<UserDefectResponseDto> getUserRaisedDefectsForAdmin(String status) {
        return adminRepository.getUserRaisedDefectsForAdmin()
                .stream()
                .peek(defect -> {
                    if (defect.getDefectStatus().equalsIgnoreCase(RaiseRequestStatus.COMPLETED.name()) ||
                                defect.getDefectStatus().equalsIgnoreCase(RaiseRequestStatus.IGNORED.name())) {
                        defect.setReferenceNumber(defect.getReferenceNumber().substring(4));
                    }
                })
                .toList();
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void updateDefectStatus(Long defectId, String status, String reason, Long adminUserId) {
        ContactUs userDefect = contactUsRepository.findById(defectId).orElseThrow(() -> new ResourceNotFoundException("User defect details not found"));
        ProfileModel userProfile = profileRepository.findByUserEmail(userDefect.getEmail()).orElseThrow(() -> new ResourceNotFoundException(USER_PROFILE_NOT_FOUND));
        if (userDefect.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.COMPLETED.name()) ||
                userDefect.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.IGNORED.name()) ||
                userDefect.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.CANCELLED.name())) {
            throw new ScenarioNotPossibleException("Action already done");
        }
        ContactUsHist userDefectHist = new ContactUsHist();
        if (status.equalsIgnoreCase("Solved")) {
            userDefect.setRequestStatus(RaiseRequestStatus.COMPLETED.name());
            userDefect.setRequestActive(Boolean.FALSE);
            userDefect.setReferenceNumber("COM_" + userDefect.getReferenceNumber());
            userDefect.setCompletedTime(LocalDateTime.now());
            userDefect.setVerified(Boolean.TRUE);

            userDefectHist.setMessage("Development team completed, Admin has been approved");
            userDefectHist.setContactUsId(userDefect.getId());
            userDefectHist.setRequestReason(RequestReason.USER_DEFECT_UPDATE.name());
            userDefectHist.setRequestStatus(RaiseRequestStatus.COMPLETED.name());
            userDefectHist.setUpdatedTime(userDefect.getCompletedTime());
            userDefectHist.setUpdatedBy(adminUserId);

            ScheduleNotification scheduleNotification = new ScheduleNotification();
            scheduleNotification.setSubject("Admin solved your concern");
            scheduleNotification.setDescription("Status: Completed" + " | " + "Reference Number: " + userDefect.getReferenceNumber().substring(4));
            functionCallForNotificationToUser(scheduleNotification, adminUserId, userDefect.getEmail());
            applicationEventPublisher.publishEvent(new NotificationQueueDto(NotificationQueueEnum.USER_DEFECT_STATUS_MAIL.name(), userProfile.getName() + "<|>" + userDefect.getReferenceNumber().substring(4) + "<|>" + userDefectHist.getMessage() + "<|>" + userDefect.getEmail().trim()));
        } else if (status.equalsIgnoreCase("Pend")) {
            userDefect.setRequestStatus(RaiseRequestStatus.PENDED.name());
            userDefectHist.setMessage("Admin kept in Pended state. Need some accuracy");
            userDefectHist.setContactUsId(userDefect.getId());
            userDefectHist.setRequestReason(RequestReason.USER_DEFECT_UPDATE.name());
            userDefectHist.setRequestStatus(RaiseRequestStatus.PENDED.name());
            userDefectHist.setUpdatedTime(LocalDateTime.now());
            userDefectHist.setUpdatedBy(adminUserId);
        } else if (status.equalsIgnoreCase("Ignore")) {
            userDefect.setRequestStatus(RaiseRequestStatus.IGNORED.name());
            userDefect.setRequestActive(false);
            userDefect.setReferenceNumber("COM_" + userDefect.getReferenceNumber());
            userDefect.setCompletedTime(LocalDateTime.now());
            userDefect.setVerified(true);

            userDefectHist.setMessage("Admin ignored. Reason: " + reason);
            userDefectHist.setContactUsId(userDefect.getId());
            userDefectHist.setRequestReason(RequestReason.USER_DEFECT_UPDATE.name());
            userDefectHist.setRequestStatus(RaiseRequestStatus.IGNORED.name());
            userDefectHist.setUpdatedTime(userDefect.getCompletedTime());
            userDefectHist.setUpdatedBy(adminUserId);

            ScheduleNotification scheduleNotification = new ScheduleNotification();
            scheduleNotification.setSubject("Admin Ignored your concern");
            scheduleNotification.setDescription("Status: Ignored" + " | " + "Reference Number: " + userDefect.getReferenceNumber().substring(4) + " | " + "Reason: " + reason);
            functionCallForNotificationToUser(scheduleNotification, adminUserId, userDefect.getEmail());
            applicationEventPublisher.publishEvent(new NotificationQueueDto(NotificationQueueEnum.USER_DEFECT_STATUS_MAIL.name(), userProfile.getName() + "<|>" + userDefect.getReferenceNumber().substring(4) + "<|>" + userDefectHist.getMessage() + "<|>" + userDefect.getEmail().trim()));
        }
        contactUsRepository.save(userDefect);
        contactUsHistRepository.save(userDefectHist);
    }

    private void functionCallForNotificationToUser(ScheduleNotification scheduleNotification, Long adminUserId, String email) {
        scheduleNotification.setScheduleFrom(LocalDateTime.now());
        scheduleNotification.setScheduleTo(scheduleNotification.getScheduleFrom().plusDays(30));
        scheduleNotification.setScheduleBy(adminUserId);
        scheduleNotification.setUpdatedBy(adminUserId);
        scheduleNotification.setRecipients(email);
        scheduleNotification.setNotificationType(SchedulingNotificationType.USER_RAISED_DEFECT.name());
        userCommonService.saveUserNotificationsForParticularUsers(email, scheduleNotificationRepository.save(scheduleNotification).getId());
    }

    @Override
    public Map<Integer, Integer> getUserMonthlyCountInAYear(int year, String status) {
        return adminRepository.getUserMonthlyCountInAYear(year, status);
    }

    @Override
    public UserProfileAndRequestDetailsDto getCompleteUserDetailsForAdmin(String username, Long adminUserId) {
        UserProfileAndRequestDetailsDto userDetails = adminRepository.getCompleteUserDetailsForAdmin(username);
        List<ContactUs> allUserRequests = contactUsRepository.findByEmail(username);

        AdminUserRequestsCountDto saveCountDto = new AdminUserRequestsCountDto();

        if (userDetails.getIsBlocked().equals(Boolean.TRUE)) userDetails.setUserStatus(UserStatus.BLOCKED.name());
        else if (userDetails.getIsDeleted().equals(Boolean.TRUE)) userDetails.setUserStatus(UserStatus.DELETED.name());
        else userDetails.setUserStatus(UserStatus.ACTIVE.name());


        functionCallToAddNameChangeRequestDetailsHistory(userDetails, allUserRequests, saveCountDto, adminUserId);
        functionCallToAddUnblockRequestDetailsHistory(userDetails, allUserRequests, saveCountDto, adminUserId);
        functionCallToAddAccRetrievalRequestDetailsHistory(userDetails, allUserRequests, saveCountDto, adminUserId);
        functionCallToAddUserPasswordChangeHistory(userDetails);
        functionCallToAddUserForgotPasswordHistory(userDetails);
        functionCallToAddUserRaisedRequestsHistory(allUserRequests, userDetails, adminUserId);
        functionCallToAddGmailSyncCountIncreaseHistory(allUserRequests, adminUserId, userDetails);

        userDetails.setUserRequestCount(saveCountDto);
        userDetails.setAccountCreationSource(Objects.requireNonNull(LoginMode.fromCode(userDetails.getLoginCodeValue())).name());
        return userDetails;
    }

    @Override
    public List<UserFeedbackResponseDto> getUserFeedbackListForAdmin() {
        AtomicInteger i = new AtomicInteger(1);
        return adminRepository.getUserFeedbackListForAdmin()
                .stream()
                .peek(feedback -> {
                    feedback.setRating(Integer.parseInt(feedback.getDescription().substring(0,1)));
                    feedback.setMessage(feedback.getDescription().substring(2));
                    feedback.setId(i.getAndIncrement());
                    feedback.setTimeOfFeedback(Timestamp.valueOf(feedback.getTimeOfFeedback().toLocalDateTime()));
                }).toList();
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void updateUserFeedback(Long feedbackId, Long adminUserId) {
        Optional<ContactUs> userFeedback = contactUsRepository.findById(feedbackId);
        if(userFeedback.isEmpty()){
            throw new ResourceNotFoundException("Feedback with id " + feedbackId + " is not found");
        }
        userFeedback.get().setRequestStatus(RaiseRequestStatus.COMPLETED.name());
        userFeedback.get().setCompletedTime(LocalDateTime.now());
        userFeedback.get().setVerified(true);
        userFeedback.get().setRequestActive(false);
        ContactUs savedUserFeedback = contactUsRepository.save(userFeedback.get());

        ContactUsHist userFeedbackHist = new ContactUsHist();
        userFeedbackHist.setContactUsId(savedUserFeedback.getId());
        userFeedbackHist.setUpdatedTime(savedUserFeedback.getCompletedTime());
        userFeedbackHist.setMessage("Admin has been viewed & Closed");
        userFeedbackHist.setRequestReason(RequestReason.USER_FEEDBACK_UPDATE.name());
        userFeedbackHist.setRequestStatus(RaiseRequestStatus.COMPLETED.name());
        userFeedbackHist.setUpdatedBy(adminUserId);
        contactUsHistRepository.save(userFeedbackHist);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void addReasonsForUserReasonDialog(ReasonDetailsRequestDto requestDto, Long adminUserId) {
        if(requestDto.getReasonCode() == null || requestDto.getReason() == null || requestDto.getReason().isEmpty()){
            throw new ScenarioNotPossibleException("Please add details correctly");
        }
        reasonDetailsRepository.findByReasonCode(requestDto.getReasonCode()).forEach(reasons -> {
            if(reasons.getReason().trim().equalsIgnoreCase(requestDto.getReason().trim()) && !reasons.getIsDeleted()){
                throw new ScenarioNotPossibleException("Reason already exists");
            }
        });
        ReasonDetails reasonDetails = new ReasonDetails();
        reasonDetails.setReason(requestDto.getReason().trim());
        reasonDetails.setReasonCode(requestDto.getReasonCode());
        reasonDetails.setCreatedBy(adminUserId);
        reasonDetails.setUpdatedBy(adminUserId);
        reasonDetailsRepository.save(reasonDetails);
    }

    @Override
    public List<ReasonListResponseDto> getAllReasonsBasedOnReasonCode(int reasonCode) {
        AtomicInteger i = new AtomicInteger(1);
        return reasonDetailsRepository.findAll()
                .stream()
                .filter(reasonDetails -> reasonDetails.getReasonCode() == reasonCode)
                .filter(reasonDetails ->  !reasonDetails.getIsDeleted())
                .map(reasonDetails -> new ReasonListResponseDto(
                        i.getAndIncrement(),
                        reasonDetails.getId(),
                        reasonDetails.getReason(),
                        reasonDetails.getUpdatedTime() == null ? reasonDetails.getCreatedTime() : reasonDetails.getUpdatedTime()
                ))
                .toList();
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void updateReasonsForUserReasonDialogByReasonCode(ReasonUpdateRequestDto requestDto, Long adminUserId) {
        if(requestDto.getReason() == null || requestDto.getReason().isEmpty()){
            throw new ScenarioNotPossibleException("Please add details correctly");
        }
        ReasonDetails reasonDetails = reasonDetailsRepository.findById(requestDto.getReasonId())
                .orElseThrow(() -> new ResourceNotFoundException("Reason with id " + requestDto.getReasonId() + " is not found"));
        reasonDetails.setReason(requestDto.getReason());
        reasonDetails.setUpdatedTime(LocalDateTime.now());
        reasonDetails.setUpdatedBy(adminUserId);
        reasonDetailsRepository.save(reasonDetails);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void deleteReasonByReasonId(int reasonId, Long adminUserId) {
        ReasonDetails reasonDetails = reasonDetailsRepository.findById(reasonId)
                .orElseThrow(() -> new ResourceNotFoundException("Reason with id " + reasonId + " is not found"));
        reasonDetails.setIsDeleted(true);
        reasonDetails.setUpdatedTime(LocalDateTime.now());
        reasonDetails.setUpdatedBy(adminUserId);
        reasonDetailsRepository.save(reasonDetails);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void blockTheUserAccountByAdmin(String email, String reason, MultipartFile file, Long adminUserId) throws JsonProcessingException {
        if (email == null || email.trim().isEmpty() || reason == null || reason.trim().isEmpty()) {
            throw new ScenarioNotPossibleException("Please provide all the details correctly");
        }
        UserAuthModel user = convertUserAuthInterfaceToDto(profileRepository.getUserDetailsByUsername(email).orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND)));
        if (user.isBlocked()) {
            throw new ScenarioNotPossibleException("User account is already blocked");
        }
        if (user.isDeleted()) {
            throw new ScenarioNotPossibleException("User account is deleted, can't block the user");
        }
        profileRepository.updateUserAuthTableWithBlockOrUnblockStatus(user.getId(), Boolean.TRUE);

        ProfileModel userProfile = profileRepository.findByUserId(user.getId()).orElseThrow(() -> new ResourceNotFoundException(USER_PROFILE_NOT_FOUND));
        LocalDateTime currentTime = LocalDateTime.now();
        ContactUs contactUs = new ContactUs();
        contactUs.setEmail(email);
        contactUs.setRequestReason(RequestReason.ACCOUNT_BLOCK_REQUEST.name());
        contactUs.setRequestActive(Boolean.TRUE);
        contactUs.setVerified(Boolean.FALSE);
        contactUs.setRequestStatus(RaiseRequestStatus.COMPLETED.name());
        contactUs.setStartTime(currentTime);
        contactUs.setCompletedTime(currentTime);
        contactUs.setReferenceNumber(StringConstants.generateAlphabetCode() + generateVerificationCode());
        ContactUs savedContactUs = contactUsRepository.save(contactUs);

        ContactUsHist contactUsHist = new ContactUsHist();
        contactUsHist.setContactUsId(savedContactUs.getId());
        contactUsHist.setName(userProfile.getName());
        contactUsHist.setMessage(BLOCKED_BY_ADMIN + ", " + reason);
        contactUsHist.setRequestReason(savedContactUs.getRequestReason());
        contactUsHist.setRequestStatus(savedContactUs.getRequestStatus());
        contactUsHist.setUpdatedTime(currentTime);
        contactUsHist.setUpdatedBy(adminUserId);
        contactUsHistRepository.save(contactUsHist);
        methodToUpdateUserAuthHistTable(user.getId(), reasonCodeIdAssociation.get(ReasonEnum.BLOCK_ACCOUNT), reason, adminUserId, LocalDateTime.now());
        if (ObjectUtils.isNotEmpty(file))
            applicationEventPublisher.publishEvent(new NotificationQueueDto(NotificationQueueEnum.ADMIN_BLOCKED_USER_MAIL.name(), objectMapper.writeValueAsString(new AdminBlockUserDto(email, reason, userProfile.getName(), convertMultipartFileToPdfBytes(file)))));
    }

    @Override
    public Map<String, List<UserDefectHistDetailsResponseDto>> getUserDefectHistDetails(List<Long> defectIds) {
        if(defectIds.isEmpty()){
            throw new ScenarioNotPossibleException("Defect Ids list can't be empty");
        }
        Map<String, List<UserDefectHistDetailsResponseDto>> responseMap = new HashMap<>();
        defectIds.forEach(defectId -> {
            ContactUs defect = contactUsRepository.findById(defectId).orElseThrow(() -> new ResourceNotFoundException("Defect with id " + defectId + " is not found"));
            List<ContactUsHist> defectHistList = contactUsHistRepository.findByContactUsId(defectId);
            if(defectHistList.isEmpty()){
                throw new ResourceNotFoundException("Defect history with defect id " + defectId + " is not found");
            }
            List<UserDefectHistDetailsResponseDto> responseList = new ArrayList<>();
            defectHistList.forEach(defectHistData -> {
                responseList.add(new UserDefectHistDetailsResponseDto(defectHistData.getRequestStatus(), defectHistData.getMessage(), defectHistData.getUpdatedTime()));
            });
            responseMap.put(defectId + "+" + (defect.getReferenceNumber().startsWith("COM_") ? defect.getReferenceNumber().substring(4) : defect.getReferenceNumber()), responseList);
        });
        return responseMap;
    }

    @Override
    public List<String> getUsernamesOfAllUsers() {
        return commonServiceRepository.findAllUsernamesOfUsers();
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void scheduleNotification(ScheduleNotificationRequestDto requestDto, Long adminUserId) {
        AdminValidations.validateScheduleNotificationRequestDetails(requestDto);
        ScheduleNotification scheduleNotification = new ScheduleNotification();
        BeanUtils.copyProperties(requestDto, scheduleNotification);
        scheduleNotification.setScheduleBy(adminUserId);
        scheduleNotification.setUpdatedBy(adminUserId);
        scheduleNotification.setNotificationType(ADMIN_SCHEDULING.name());
        ScheduleNotification response = scheduleNotificationRepository.save(scheduleNotification);
        functionToSaveNotificationToUsers(requestDto.getRecipients(), response.getId());
    }

    @Override
    public List<AdminSchedulesResponseDto> getAllActiveSchedulesOfAdmin(String status, String operationMode) {
        return adminRepository.getAllActiveSchedulesOfAdmin(status, operationMode.toUpperCase())
                .stream()
                .map(schedule -> {
                    if (schedule.getRecipients().equalsIgnoreCase(TargetUsersForScheduleNotification.ALL.name())) {
                        schedule.setRecipients(TargetUsersForScheduleNotification.ALL.getTargetUser());
                    } else {
                        schedule.setRecipentList(Arrays.stream(schedule.getRecipients().split(",")).toList());
                        schedule.setRecipients(TargetUsersForScheduleNotification.SPECIFIC.getTargetUser());
                    }
                    return schedule;
                }).toList();
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void cancelTheUserScheduling(Long scheduleId, Long adminUserId) {
        ScheduleNotification notification = scheduleNotificationRepository.findById(scheduleId).orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + scheduleId));
        if (!notification.getNotificationType().equalsIgnoreCase(ADMIN_SCHEDULING.name())) throw new ScenarioNotPossibleException("Automated schedules can't be cancelled");

        if (Boolean.TRUE.equals(notification.isCancelled())) {
            throw new IllegalStateException("Schedule with id " + scheduleId + " is already cancelled.");
        }
        notification.setCancelled(true);
        notification.setUpdatedAt(LocalDateTime.now());
        notification.setUpdatedBy(adminUserId);
        scheduleNotificationRepository.save(notification);
        new Thread(() -> userNotificationRepository.deleteAllByScheduleId(scheduleId)).start();
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void updateAdminPlacedSchedules(AdminScheduleRequestDto requestDto, Long adminUserId) {
        ScheduleNotification notification = scheduleNotificationRepository.findById(requestDto.getScheduleId()).orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + requestDto.getScheduleId()));
        if (!notification.getNotificationType().equalsIgnoreCase(ADMIN_SCHEDULING.name())) throw new ScenarioNotPossibleException("Automated schedules can't be updated");

        AdminValidations.validateScheduleNotificationRequestDetails(new ScheduleNotificationRequestDto(requestDto.getSubject(), requestDto.getDescription(), requestDto.getScheduleFrom(), requestDto.getScheduleTo(), requestDto.getRecipients()));

        notification.setSubject(requestDto.getSubject());
        notification.setDescription(requestDto.getDescription());
        notification.setScheduleFrom(requestDto.getScheduleFrom());
        notification.setScheduleTo(requestDto.getScheduleTo());
        notification.setRecipients(requestDto.getRecipients());
        notification.setDescription(notification.getDescription());
        notification.setUpdatedAt(LocalDateTime.now());
        notification.setUpdatedBy(adminUserId);
        notification.setCancelled(false);
        notification.setActive(true);
        scheduleNotificationRepository.save(notification);
        functionToSaveNotificationToUsers(requestDto.getRecipients(), requestDto.getScheduleId());
        new Thread(() -> userNotificationRepository.deleteAllByScheduleId(requestDto.getScheduleId())).start();
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void deleteUserScheduling(Long scheduleId, Long adminUserId) {
        ScheduleNotification notification = scheduleNotificationRepository.findById(scheduleId).orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + scheduleId));
        if (!notification.getNotificationType().equalsIgnoreCase(ADMIN_SCHEDULING.name())) throw new ScenarioNotPossibleException("Automated schedules can't be deleted");

        notification.setActive(false);
        notification.setUpdatedBy(adminUserId);
        notification.setUpdatedAt(LocalDateTime.now());
        scheduleNotificationRepository.save(notification);
        new Thread(() -> userNotificationRepository.deleteAllByScheduleId(scheduleId)).start();
    }

    @Override
    @Transactional
    public void uploadExcelTemplate(Long adminUserId, String type, String operation, MultipartFile file) throws IOException {
        String fileName = "";
        if ("profile-template".equalsIgnoreCase(type)) fileName = StringConstants.PROFILE_TEMPLATE_EXCEL_NAME;

        if ("Update".equalsIgnoreCase(operation)) {
            functionCallToUpdateExcelTemplate(adminUserId, fileName, file);
        } else if ("Upload".equalsIgnoreCase(operation)) {
            functionCallToUploadExcelTemplate(adminUserId, fileName, file);
        } else throw new ScenarioNotPossibleException("Operation type is invalid");
    }

    @Override
    public List<ExcelTemplateList> getAllExcelTemplates() {
        List<ExcelTemplateListProjection> templatesList = excelTemplateRepository.getAllExcelTemplateList();
        return templatesList.stream()
                .map(template ->
                        ExcelTemplateList.builder()
                            .excelFile(template.getExcelFile())
                            .excelType(template.getExcelType())
                            .createdBy(template.getCreatedBy())
                            .createdAt(template.getCreatedAt())
                            .updatedBy(template.getUpdatedBy())
                            .updatedAt(template.getUpdatedAt())
                            .build()
                ).toList();
    }

    private void functionCallToUpdateExcelTemplate(Long adminUserId, String fileName, MultipartFile file) throws IOException {
        functionCallToUploadExelTemplateToCloud(file, fileName);
        excelTemplateRepository.save(excelTemplateRepository.findByName(fileName).orElseThrow(() -> new ResourceNotFoundException(TEMPLATE_NOT_FOUND)).toBuilder()
                .name(fileName)
                .content(file.getBytes())
                .contentType(file.getContentType())
                .updatedBy(adminUserId)
                .updatedTime(LocalDateTime.now())
                .build()
        );
    }

    private void functionCallToUploadExcelTemplate(Long adminUserId, String fileName, MultipartFile file) {
        Optional<ExcelTemplate> excelTemplate = excelTemplateRepository.findByName(fileName);
        if (excelTemplate.isPresent()) {
            throw new ScenarioNotPossibleException(EXCEL_TEMPLATE_EXIST_MESSAGE);
        }

        try {
            functionCallToUploadExelTemplateToCloud(file, fileName);
            excelTemplateRepository.save(ExcelTemplate.builder()
                    .name(fileName)
                    .content(file.getBytes())
                    .contentType(file.getContentType())
                    .createdBy(adminUserId)
                    .updatedBy(adminUserId)
                    .build()
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new FileUploadException("Failed to Upload");
        }
    }

    private void functionCallToUploadExelTemplateToCloud(MultipartFile file, String fileName) {
        if (LOCAL_PROFILE.equalsIgnoreCase(activeProfile)) {
            cloudinaryService.uploadExcelTemplateToCloudinary(file, fileName);
        } else {
            awsServices.uploadExcelTemplateToS3(file, fileName);
        }
    }

    private void functionToSaveNotificationToUsers(String recipients, Long scheduleId) {
        if (!recipients.equalsIgnoreCase(ALL)) {
            userCommonService.saveUserNotificationsForParticularUsers(recipients, scheduleId);
        } else {
            /** Currently using @Async batch process for saving notifications for all the users.
             * For more traffic of users, It is advisable to use direct insert queries.
             * Else For more efficient approach, we can use Kafka or any messaging queue to handle such huge number of people scenarios.
             */
            userCommonService.saveUserNotificationsForAllUsers(getUsernamesOfAllUsers(), scheduleId);
        }
    }

    private byte[] generateExcelReport(List<UserGridDto> userGridDtoList){
        try(Workbook workbook = new XSSFWorkbook()){
            Sheet sheet = workbook.createSheet("User Details Report");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"S No", "Name", "Username", "Phone", "Created Time", "Date of Birth"};
            for(int i=0; i< headers.length; i++){
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(createHeaderStyle(workbook));
            }
            CellStyle dateStyle = createDateStyle(workbook);
            int rowIndex = 1;
            for (UserGridDto data : userGridDtoList) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(data.getSlNo());
                row.createCell(1).setCellValue(data.getName());
                row.createCell(2).setCellValue(data.getUsername());
                row.createCell(3).setCellValue(data.getPhone()!=null?data.getPhone():"-");

                Cell dateCell = row.createCell(4);
                dateCell.setCellValue(data.getCreatedDateTime());
                dateCell.setCellStyle(dateStyle);

                Cell dateCell2 = row.createCell(5);
                dateCell2.setCellValue(data.getDateOfBirth());
                dateCell2.setCellStyle(dateStyle);
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            throw new ResourceNotFoundException("Error in generating excel report");
        }
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle dateStyle = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy"));
        return dateStyle;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private void functionCallToChangeDetails(UserAuthModel user, ProfileModel userProfile, String email, ContactUs contactUs, String requestStatus, Long adminUserId, int gmailSyncRequestCount) throws JsonProcessingException {
        ContactUsHist requestUserHist = new ContactUsHist();

        if (requestStatus.equalsIgnoreCase(RequestReason.ACCOUNT_UNBLOCK_REQUEST.name())) {
            profileRepository.updateUserAuthTableWithBlockOrUnblockStatus(user.getId(), false);
            ContactUsHist requestDetailsHist = contactUsHistRepository.findByContactUsIdList(contactUs.getId())
                    .stream()
                    .filter(request -> request.getRequestReason().equalsIgnoreCase(RequestReason.ACCOUNT_UNBLOCK_REQUEST.name()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException(REQUEST_NOT_FOUND));
            LocalDateTime completedTime = LocalDateTime.now();
            methodToUpdateUserAuthHistTable(user.getId(), reasonCodeIdAssociation.get(ReasonEnum.UNBLOCK_ACCOUNT), requestDetailsHist.getMessage(), adminUserId, completedTime);
            requestUserHist.setRequestReason(RequestReason.ACCOUNT_UNBLOCK_REQUEST.name());
            requestUserHist.setMessage("Admin has been unblocked the Account");
            methodToUpdateContactUsTable(contactUs, requestUserHist, completedTime, adminUserId);
            applicationEventPublisher.publishEvent(new NotificationQueueDto(NotificationQueueEnum.USER_ACCOUNT_UNBLOCK_REQUEST_SUCCESSFUL_MAIL.name(), userProfile.getName() + "<|>" + email + "<|>" + contactUs.getReferenceNumber().substring(4) + "<|>" + "Unblocked successfully"));
        } else if (requestStatus.equalsIgnoreCase(RequestReason.ACCOUNT_NOT_DELETE_REQUEST.name())) {
            profileRepository.updateUserAuthTableWithDeleteOrUndeleteStatus(user.getId(), false);
            ContactUsHist requestDetailsHist = contactUsHistRepository.findByContactUsIdList(contactUs.getId())
                    .stream()
                    .filter(request -> request.getRequestReason().equalsIgnoreCase(RequestReason.ACCOUNT_NOT_DELETE_REQUEST.name()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException(REQUEST_NOT_FOUND));
            LocalDateTime completedTime = LocalDateTime.now();
            methodToUpdateUserAuthHistTable(user.getId(), reasonCodeIdAssociation.get(ReasonEnum.ACCOUNT_RETRIEVAL), requestDetailsHist.getMessage(), adminUserId, completedTime);
            requestUserHist.setRequestReason(RequestReason.ACCOUNT_NOT_DELETE_REQUEST.name());
            requestUserHist.setMessage("Admin has been approved Account Retrieval");
            methodToUpdateContactUsTable(contactUs, requestUserHist, completedTime, adminUserId);
            applicationEventPublisher.publishEvent(new NotificationQueueDto(NotificationQueueEnum.USER_ACCOUNT_RETRIEVAL_REQUEST_SUCCESSFUL_MAIL.name(), userProfile.getName() + "<|>" + email + "<|>" + contactUs.getReferenceNumber().substring(4) + "<|>" + "Retrieved successfully"));
        } else if (requestStatus.equalsIgnoreCase(RequestReason.NAME_CHANGE_REQUEST.name())) {
            ContactUsHist requestDetailsHist = contactUsHistRepository.findByContactUsIdList(contactUs.getId())
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException(REQUEST_NOT_FOUND));
            if (!userProfile.getName().toLowerCase().contains(requestDetailsHist.getMessage().toLowerCase().split(",")[0])) {
                throw new ScenarioNotPossibleException("Old name didn't match");
            }
            LocalDateTime completedTime = LocalDateTime.now();
            String oldName = userProfile.getName();
            userProfile.setName(requestDetailsHist.getName());
            profileRepository.save(userProfile);
            methodToUpdateUserAuthHistTable(user.getId(), reasonCodeIdAssociation.get(ReasonEnum.NAME_CHANGE), requestDetailsHist.getMessage().split(",")[1], adminUserId, completedTime);
            requestUserHist.setRequestReason(RequestReason.NAME_CHANGE_REQUEST.name());
            requestUserHist.setMessage("Admin has been approved Name change Request");
            methodToUpdateContactUsTable(contactUs, requestUserHist, completedTime, adminUserId);

            ScheduleNotification scheduleNotification = new ScheduleNotification();
            scheduleNotification.setSubject("Admin has been approved Name change Request");
            scheduleNotification.setDescription("New Name: " + requestDetailsHist.getName() + " | " + "Old Name: " + oldName + " | " + "Reference Number: " + contactUs.getReferenceNumber().substring(4));
            scheduleNotification.setScheduleFrom(LocalDateTime.now());
            scheduleNotification.setScheduleTo(scheduleNotification.getScheduleFrom().plusDays(30));
            scheduleNotification.setScheduleBy(adminUserId);
            scheduleNotification.setUpdatedBy(adminUserId);
            scheduleNotification.setRecipients(email);
            scheduleNotification.setNotificationType(SchedulingNotificationType.NAME_CHANGE_REQUEST.name());
            userCommonService.saveUserNotificationsForParticularUsers(user.getUsername(), scheduleNotificationRepository.save(scheduleNotification).getId());
            applicationEventPublisher.publishEvent(new NotificationQueueDto(NotificationQueueEnum.NAME_CHANGE_REQUEST_APPROVED_MAIL.name(), userProfile.getName() + "<|>" + email + "<|>" + contactUs.getReferenceNumber().substring(4)));
        } else if (requestStatus.equalsIgnoreCase(RequestReason.GMAIL_SYNC_REQUEST_TYPE.name())) {
            if (gmailSyncRequestCount <= 0 || gmailSyncRequestCount > 3) {
                throw new ScenarioNotPossibleException("Please enter valid count between 0 to 3");
            }

            int rowsAffected = profileRepository.updateGmailSyncCountByUserRequest(user.getId(), 3 - gmailSyncRequestCount);
            log.info("Rows Affected {}", rowsAffected);

            requestUserHist.setRequestReason(RequestReason.GMAIL_SYNC_REQUEST_TYPE.name());
            requestUserHist.setMessage("Admin has been approved with " + gmailSyncRequestCount + " sync chance/chances");
            methodToUpdateContactUsTable(contactUs, requestUserHist, LocalDateTime.now(), adminUserId);

            ScheduleNotification scheduleNotification = new ScheduleNotification();
            scheduleNotification.setSubject("Gmail Sync Count Increase Request Approved");
            scheduleNotification.setDescription(objectMapper.writeValueAsString(new GmailSyncCountJsonDto(gmailSyncRequestCount, STATUS_APPROVED, contactUs.getId())));
            functionToScheduleGmailSyncUpdateToUser(scheduleNotification, adminUserId, email, user, contactUs);
            applicationEventPublisher.publishEvent(new NotificationQueueDto(NotificationQueueEnum.GMAIL_SYNC_APPROVED_USER_MAIL.name(), userProfile.getName() + "<|>" + email + "<|>" + gmailSyncRequestCount));
        }
    }

    private void methodToUpdateContactUsTable(ContactUs contactUs, ContactUsHist requestUserHist, LocalDateTime completedTime, Long adminUserId){
        contactUs.setRequestActive(false);
        contactUs.setVerified(true);
        contactUs.setReferenceNumber("COM_" + contactUs.getReferenceNumber());
        contactUs.setRequestStatus(RaiseRequestStatus.COMPLETED.name());
        contactUs.setCompletedTime(completedTime);
        ContactUs savedRequest = contactUsRepository.save(contactUs);

        requestUserHist.setContactUsId(savedRequest.getId());
        requestUserHist.setRequestStatus(RaiseRequestStatus.COMPLETED.name());
        requestUserHist.setUpdatedTime(completedTime);
        requestUserHist.setUpdatedBy(adminUserId);
        contactUsHistRepository.save(requestUserHist);
    }

    private void methodToUpdateUserAuthHistTable(Long userId, int reasonTypeId, String comment, Long updatedUserId, LocalDateTime completedTime){
        profileRepository.insertUserAuthHistory(userId, completedTime, reasonTypeId, comment, updatedUserId);
    }

    private void functionCallToDeclineTheUserRequest(UserAuthModel user, ProfileModel userProfile, ContactUs contactUs, String declineReason, String email, Long adminUserId, int gmailSyncRequestCount) throws JsonProcessingException {
        if (StringUtils.isBlank(declineReason)) {
            throw new ScenarioNotPossibleException("Decline reason should not be empty");
        }
        contactUs.setCompletedTime(LocalDateTime.now());
        contactUs.setRequestActive(Boolean.FALSE);
        contactUs.setVerified(Boolean.TRUE);
        contactUs.setReferenceNumber("COM_" + contactUs.getReferenceNumber());
        contactUs.setRequestStatus(RaiseRequestStatus.CANCELLED.name());
        ContactUs response = contactUsRepository.save(contactUs);

        ContactUsHist contactUsHist = new ContactUsHist();
        contactUsHist.setContactUsId(response.getId());
        contactUsHist.setMessage(declineReason);
        contactUsHist.setRequestReason(response.getRequestReason());
        contactUsHist.setRequestStatus(response.getRequestStatus());
        contactUsHist.setUpdatedTime(response.getCompletedTime());
        contactUsHist.setUpdatedBy(adminUserId);
        contactUsHistRepository.save(contactUsHist);

        if (contactUs.getRequestReason().equalsIgnoreCase(RequestReason.GMAIL_SYNC_REQUEST_TYPE.name())) {
            ScheduleNotification scheduleNotification = new ScheduleNotification();
            scheduleNotification.setSubject("Gmail Sync Count Increase Request Rejected");
            scheduleNotification.setDescription(objectMapper.writeValueAsString(new GmailSyncCountJsonDto(0, STATUS_REJECTED + ". Reason: " + declineReason, contactUs.getId())));
            functionToScheduleGmailSyncUpdateToUser(scheduleNotification, adminUserId, email, user, contactUs);
            applicationEventPublisher.publishEvent(new NotificationQueueDto(NotificationQueueEnum.GMAIL_SYNC_REJECTED_USER_MAIL.name(), userProfile.getName() + "<|>" + email));
        } else if (contactUs.getRequestReason().equalsIgnoreCase(RequestReason.NAME_CHANGE_REQUEST.name())) {
            ScheduleNotification scheduleNotification = new ScheduleNotification();
            scheduleNotification.setSubject("Admin declined Name change Request");
            scheduleNotification.setDescription("Reference Number: " + contactUs.getReferenceNumber().substring(4) + " | " + "Status: DECLINED" + " | " + "Reason: " + declineReason);
            scheduleNotification.setScheduleFrom(LocalDateTime.now());
            scheduleNotification.setScheduleTo(scheduleNotification.getScheduleFrom().plusDays(30));
            scheduleNotification.setScheduleBy(adminUserId);
            scheduleNotification.setUpdatedBy(adminUserId);
            scheduleNotification.setRecipients(email);
            scheduleNotification.setNotificationType(SchedulingNotificationType.NAME_CHANGE_REQUEST.name());
            userCommonService.saveUserNotificationsForParticularUsers(user.getUsername(), scheduleNotificationRepository.save(scheduleNotification).getId());
            applicationEventPublisher.publishEvent(new NotificationQueueDto(NotificationQueueEnum.NAME_CHANGE_REQUEST_REJECTED_MAIL.name(), userProfile.getName() + "<|>" + email + "<|>" + contactUs.getReferenceNumber().substring(4)));
        } else if (contactUs.getRequestReason().equalsIgnoreCase(RequestReason.ACCOUNT_UNBLOCK_REQUEST.name())) {
            applicationEventPublisher.publishEvent(new NotificationQueueDto(NotificationQueueEnum.USER_ACCOUNT_UNBLOCK_REQUEST_REJECTED_MAIL.name(), userProfile.getName() + "<|>" + email + "<|>" + contactUs.getReferenceNumber().substring(4) + "<|>" + "Unblock Request" + "<|>" + declineReason));
        } else if (contactUs.getRequestReason().equalsIgnoreCase(RequestReason.ACCOUNT_NOT_DELETE_REQUEST.name())) {
            applicationEventPublisher.publishEvent(new NotificationQueueDto(NotificationQueueEnum.USER_ACCOUNT_RETRIEVAL_REQUEST_REJECTED_MAIL.name(), userProfile.getName() + "<|>" + email + "<|>" + contactUs.getReferenceNumber().substring(4) + "<|>" + "Retrieval Request" + "<|>" + declineReason));
        }
    }

    private ScheduleNotification getUserScheduledGmailSyncNotification(UserAuthModel user, ContactUs contactUs) {
        return scheduleNotificationRepository.findByScheduleBy(user.getId()).stream()
                .filter(schedule -> {
                    try {
                        return schedule.getNotificationType().equalsIgnoreCase(SchedulingNotificationType.GMAIL_SYNC_COUNT_INCREASE.name())
                                && schedule.getCreatedDate().toLocalDate().equals(LocalDate.now())
                                && Objects.equals(objectMapper.readValue(schedule.getDescription(), GmailSyncCountJsonDto.class).getContactUsId(), contactUs.getId());
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }).findFirst().orElseThrow(() -> new ResourceNotFoundException("Schedule Gmail Sync request not found"));
    }

    private void functionToScheduleGmailSyncUpdateToUser(ScheduleNotification scheduleNotification, Long adminUserId, String email, UserAuthModel user, ContactUs contactUs) {
        scheduleNotification.setUpdatedBy(adminUserId);
        scheduleNotification.setScheduleFrom(LocalDateTime.now());
        scheduleNotification.setScheduleTo(LocalDate.now().atTime(LocalTime.MAX));
        scheduleNotification.setScheduleBy(adminUserId);
        scheduleNotification.setParentKey(getUserScheduledGmailSyncNotification(user, contactUs).getId());
        scheduleNotification.setRecipients(email);
        scheduleNotification.setNotificationType(SchedulingNotificationType.GMAIL_SYNC_COUNT_INCREASE.name());
        userCommonService.saveUserNotificationsForParticularUsers(user.getUsername(), scheduleNotificationRepository.save(scheduleNotification).getId());
    }

    private void functionCallToAddNameChangeRequestDetailsHistory(UserProfileAndRequestDetailsDto userDetails, List<ContactUs> allUserRequests, AdminUserRequestsCountDto saveCountDto, Long adminUserId) {
        AtomicInteger nameChangeActiveRequestsCount = new AtomicInteger(0);
        AtomicInteger nameChangeCompletedRequestsCount = new AtomicInteger(0);
        AtomicInteger nameChangeDeclinedRequestsCount = new AtomicInteger(0);
        allUserRequests.stream()
                .filter(nameChangeRequest -> nameChangeRequest.getRequestReason().equalsIgnoreCase(RequestReason.NAME_CHANGE_REQUEST.name()))
                .sorted((a, b) -> a.getStartTime().compareTo(b.getStartTime()))
                .forEach(request -> {
                    if (request.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.SUBMITTED.name()) || request.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.INITIATED.name())) {
                        nameChangeActiveRequestsCount.getAndIncrement();
                    } else if (request.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.COMPLETED.name())) {
                        nameChangeCompletedRequestsCount.getAndIncrement();
                    } else if (request.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.CANCELLED.name())) {
                        nameChangeDeclinedRequestsCount.getAndIncrement();
                    }
                    AdminUserNameChangeDetailsDto dto = new AdminUserNameChangeDetailsDto();
                    Map<RaiseRequestStatus, UserRequestsUpdatedHistDto> requestTimeStatusHistoryMap = new HashMap<>();
                    Map<String, String> approvedOrRejectedMap = new HashMap<>();
                    List<ContactUsHist> nameChangeRequestHistList = contactUsHistRepository.findByContactUsId(request.getId());
                    nameChangeRequestHistList.forEach(nameChangeRequestHist -> {
                        if (nameChangeRequestHist.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.INITIATED.name())) {
                            requestTimeStatusHistoryMap.put(RaiseRequestStatus.INITIATED, new UserRequestsUpdatedHistDto(Timestamp.valueOf(nameChangeRequestHist.getUpdatedTime())));
                        }
                        if (nameChangeRequestHist.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.SUBMITTED.name())) {
                            dto.setOldName(nameChangeRequestHist.getMessage().split(",")[0]);
                            dto.setNewName(nameChangeRequestHist.getName());
                            dto.setReasonForNameChange(nameChangeRequestHist.getMessage().split(",")[1]);
                            requestTimeStatusHistoryMap.put(RaiseRequestStatus.SUBMITTED, new UserRequestsUpdatedHistDto(Timestamp.valueOf(nameChangeRequestHist.getUpdatedTime())));
                        }
                        if (nameChangeRequestHist.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.COMPLETED.name()) || nameChangeRequestHist.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.CANCELLED.name())) {
                            requestTimeStatusHistoryMap.put(RaiseRequestStatus.COMPLETED, new UserRequestsUpdatedHistDto(Timestamp.valueOf(nameChangeRequestHist.getUpdatedTime())));
                            if (nameChangeRequestHist.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.CANCELLED.name()))
                                approvedOrRejectedMap.put(STATUS_REJECTED, nameChangeRequestHist.getMessage());
                            else approvedOrRejectedMap.put(STATUS_APPROVED, nameChangeRequestHist.getMessage());

                            String username = Objects.requireNonNull(profileRepository.getUserAuthModelByUserId(nameChangeRequestHist.getUpdatedBy()).orElse(null)).getUsername();
                            if (!Objects.equals(nameChangeRequestHist.getUpdatedBy(), adminUserId)) dto.getRequestDoneBy().append(nameChangeRequestHist.getRequestStatus().substring(0, 1).toUpperCase()).append(nameChangeRequestHist.getRequestStatus().substring(1).toLowerCase()).append(" By: ").append(username);
                            else dto.getRequestDoneBy().append(nameChangeRequestHist.getRequestStatus().substring(0,1).toUpperCase()).append(nameChangeRequestHist.getRequestStatus().substring(1).toLowerCase()).append(" By: You" + "(").append(username).append(")");
                        }
                        dto.setApprovedOrRejected(approvedOrRejectedMap);
                        dto.setRequestTimeStatusHistory(requestTimeStatusHistoryMap);
                    });
                    dto.setReferenceNumber(request.getReferenceNumber().startsWith("COM_") ? request.getReferenceNumber().substring(4) : request.getReferenceNumber());
                    if (request.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.SUBMITTED.name()) || request.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.INITIATED.name())) {
                        dto.setRequestStatus(RaiseRequestStatus.valueOf(request.getRequestStatus()));
                        dto.setDaysTakenForCompletion(null);
                    } else {
                        dto.setDaysTakenForCompletion(Math.max(1, (int) ChronoUnit.DAYS.between(request.getStartTime(), request.getCompletedTime())));
                        dto.setRequestStatus(RaiseRequestStatus.valueOf(request.getRequestStatus()));
                    }
                    userDetails.getNameChangeRequests().add(dto);
                });
        saveCountDto.setNameChangeActiveRequests(nameChangeActiveRequestsCount);
        saveCountDto.setNameChangeCompletedRequests(nameChangeCompletedRequestsCount);
        saveCountDto.setNameChangeDeclinedRequests(nameChangeDeclinedRequestsCount);
    }

    private void functionCallToAddUnblockRequestDetailsHistory(UserProfileAndRequestDetailsDto userDetails, List<ContactUs> allUserRequests, AdminUserRequestsCountDto saveCountDto, Long adminUserId) {
        AtomicInteger accBlockActiveRequestsCount = new AtomicInteger(0);
        AtomicInteger accBlockCompletedRequestsCount = new AtomicInteger(0);
        AtomicInteger accBlockDeclinedRequestsCount = new AtomicInteger(0);
        allUserRequests.stream()
                .filter(accUnblockRequest -> accUnblockRequest.getRequestReason().equalsIgnoreCase(RequestReason.ACCOUNT_UNBLOCK_REQUEST.name()))
                .sorted((a, b) -> a.getStartTime().compareTo(b.getStartTime()))
                .forEach(request -> {
                    if (request.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.SUBMITTED.name()) || request.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.INITIATED.name())) {
                        accBlockActiveRequestsCount.getAndIncrement();
                    } else if (request.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.COMPLETED.name())) {
                        accBlockCompletedRequestsCount.getAndIncrement();
                    } else if (request.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.CANCELLED.name())) {
                        accBlockDeclinedRequestsCount.getAndIncrement();
                    }
                    AdminUserUnblockRequestDetailsDto dto = new AdminUserUnblockRequestDetailsDto();
                    Map<RaiseRequestStatus, UserRequestsUpdatedHistDto> requestTimeStatusHistoryMap = new HashMap<>();
                    Map<String, String> approvedOrRejectedMap = new HashMap<>();
                    List<ContactUsHist> unblockAccHistList = contactUsHistRepository.findByContactUsId(request.getId());
                    unblockAccHistList.forEach(accUnblockHistRequest -> {
                        if (accUnblockHistRequest.getRequestReason().equalsIgnoreCase(RequestReason.ACCOUNT_BLOCK_REQUEST.name())) {
                            String adminUsername = Objects.requireNonNull(profileRepository.getUserAuthModelByUserId(accUnblockHistRequest.getUpdatedBy()).orElse(null)).getUsername();
                            if (accUnblockHistRequest.getMessage().split(",")[0].equalsIgnoreCase(BLOCKED_BY_USER))
                                dto.setBlockedBy("USER");
                            else dto.setBlockedBy(adminUsername);
                        } else {
                            if (accUnblockHistRequest.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.INITIATED.name())) {
                                requestTimeStatusHistoryMap.put(RaiseRequestStatus.INITIATED, new UserRequestsUpdatedHistDto(Timestamp.valueOf(accUnblockHistRequest.getUpdatedTime())));
                            }
                            if (accUnblockHistRequest.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.SUBMITTED.name())) {
                                dto.setUnblockRequestReason(accUnblockHistRequest.getMessage());
                                requestTimeStatusHistoryMap.put(RaiseRequestStatus.SUBMITTED, new UserRequestsUpdatedHistDto(Timestamp.valueOf(accUnblockHistRequest.getUpdatedTime())));
                            }
                            if (accUnblockHistRequest.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.COMPLETED.name()) || accUnblockHistRequest.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.CANCELLED.name())) {
                                requestTimeStatusHistoryMap.put(RaiseRequestStatus.COMPLETED, new UserRequestsUpdatedHistDto(Timestamp.valueOf(accUnblockHistRequest.getUpdatedTime())));
                                if (accUnblockHistRequest.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.CANCELLED.name()))
                                    approvedOrRejectedMap.put(STATUS_REJECTED, accUnblockHistRequest.getMessage());
                                else approvedOrRejectedMap.put(STATUS_APPROVED, accUnblockHistRequest.getMessage());

                                String username = Objects.requireNonNull(profileRepository.getUserAuthModelByUserId(accUnblockHistRequest.getUpdatedBy()).orElse(null)).getUsername();
                                if (!Objects.equals(accUnblockHistRequest.getUpdatedBy(), adminUserId)) dto.getRequestDoneBy().append(accUnblockHistRequest.getRequestStatus().substring(0, 1).toUpperCase()).append(accUnblockHistRequest.getRequestStatus().substring(1).toLowerCase()).append(" By: ").append(username);
                                else dto.getRequestDoneBy().append(accUnblockHistRequest.getRequestStatus().substring(0,1).toUpperCase()).append(accUnblockHistRequest.getRequestStatus().substring(1).toLowerCase()).append(" By: You" + "(").append(username).append(")");
                            }
                            dto.setApprovedOrRejected(approvedOrRejectedMap);
                            dto.setRequestTimeStatusHistory(requestTimeStatusHistoryMap);
                        }
                    });
                    dto.setReferenceNumber(request.getReferenceNumber().startsWith("COM_") ? request.getReferenceNumber().substring(4) : request.getReferenceNumber());
                    if (request.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.SUBMITTED.name()) || request.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.INITIATED.name())) {
                        dto.setRequestStatus(RaiseRequestStatus.valueOf(request.getRequestStatus()));
                        dto.setDaysTakenForCompletion(null);
                    } else {
                        dto.setDaysTakenForCompletion(Math.max(1, (int) ChronoUnit.DAYS.between(request.getStartTime(), request.getCompletedTime())));
                        dto.setRequestStatus(RaiseRequestStatus.valueOf(request.getRequestStatus()));
                    }
                    userDetails.getUnblockAccountRequests().add(dto);
                });
        saveCountDto.setAccBlockActiveRequests(accBlockActiveRequestsCount);
        saveCountDto.setAccBlockChangeCompletedRequests(accBlockCompletedRequestsCount);
        saveCountDto.setAccBlockChangeDeclinedRequests(accBlockDeclinedRequestsCount);
    }

    private void functionCallToAddAccRetrievalRequestDetailsHistory(UserProfileAndRequestDetailsDto userDetails, List<ContactUs> allUserRequests, AdminUserRequestsCountDto saveCountDto, Long adminUserId) {
        AtomicInteger accRetrievalActiveRequestsCount = new AtomicInteger(0);
        AtomicInteger accRetrievalCompletedRequestsCount = new AtomicInteger(0);
        AtomicInteger accRetrievalDeclinedRequestsCount = new AtomicInteger(0);
        allUserRequests.stream()
                .filter(accUnblockRequest -> accUnblockRequest.getRequestReason().equalsIgnoreCase(RequestReason.ACCOUNT_NOT_DELETE_REQUEST.name()))
                .sorted((a, b) -> a.getStartTime().compareTo(b.getStartTime()))
                .forEach(request -> {
                    if (request.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.SUBMITTED.name()) || request.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.INITIATED.name())) {
                        accRetrievalActiveRequestsCount.getAndIncrement();
                    } else if (request.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.COMPLETED.name())) {
                        accRetrievalCompletedRequestsCount.getAndIncrement();
                    } else if (request.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.CANCELLED.name())) {
                        accRetrievalDeclinedRequestsCount.getAndIncrement();
                    }
                    AdminUserAccRetrievalRequestDetailsDto dto = new AdminUserAccRetrievalRequestDetailsDto();
                    Map<RaiseRequestStatus, UserRequestsUpdatedHistDto> requestTimeStatusHistoryMap = new HashMap<>();
                    Map<String, String> approvedOrRejectedMap = new HashMap<>();
                    List<ContactUsHist> accRetrievalHistList = contactUsHistRepository.findByContactUsId(request.getId());
                    accRetrievalHistList.forEach(accRetrievalHistRequest -> {
                        if (accRetrievalHistRequest.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.INITIATED.name())) {
                            requestTimeStatusHistoryMap.put(RaiseRequestStatus.INITIATED, new UserRequestsUpdatedHistDto(Timestamp.valueOf(accRetrievalHistRequest.getUpdatedTime())));
                        }
                        if (accRetrievalHistRequest.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.SUBMITTED.name())) {
                            dto.setAccountRetrievalRequestReason(accRetrievalHistRequest.getMessage());
                            requestTimeStatusHistoryMap.put(RaiseRequestStatus.SUBMITTED, new UserRequestsUpdatedHistDto(Timestamp.valueOf(accRetrievalHistRequest.getUpdatedTime())));
                        }
                        if (accRetrievalHistRequest.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.COMPLETED.name()) || accRetrievalHistRequest.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.CANCELLED.name())) {
                            requestTimeStatusHistoryMap.put(RaiseRequestStatus.COMPLETED, new UserRequestsUpdatedHistDto(Timestamp.valueOf(accRetrievalHistRequest.getUpdatedTime())));
                            if (accRetrievalHistRequest.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.CANCELLED.name()))
                                approvedOrRejectedMap.put(STATUS_REJECTED, accRetrievalHistRequest.getMessage());
                            else approvedOrRejectedMap.put(STATUS_APPROVED, accRetrievalHistRequest.getMessage());

                            String username = Objects.requireNonNull(profileRepository.getUserAuthModelByUserId(accRetrievalHistRequest.getUpdatedBy()).orElse(null)).getUsername();
                            if (!Objects.equals(accRetrievalHistRequest.getUpdatedBy(), adminUserId)) dto.getRequestDoneBy().append(accRetrievalHistRequest.getRequestStatus().substring(0, 1).toUpperCase()).append(accRetrievalHistRequest.getRequestStatus().substring(1).toLowerCase()).append(" By: ").append(username);
                            else dto.getRequestDoneBy().append(accRetrievalHistRequest.getRequestStatus().substring(0,1).toUpperCase()).append(accRetrievalHistRequest.getRequestStatus().substring(1).toLowerCase()).append(" By: You" + "(").append(username).append(")");
                        }
                        dto.setApprovedOrRejected(approvedOrRejectedMap);
                        dto.setRequestTimeStatusHistory(requestTimeStatusHistoryMap);
                    });
                    dto.setReferenceNumber(request.getReferenceNumber().startsWith("COM_") ? request.getReferenceNumber().substring(4) : request.getReferenceNumber());
                    if (request.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.SUBMITTED.name()) || request.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.INITIATED.name())) {
                        dto.setRequestStatus(RaiseRequestStatus.valueOf(request.getRequestStatus()));
                        dto.setDaysTakenForCompletion(null);
                    } else {
                        dto.setDaysTakenForCompletion(Math.max(1, (int) ChronoUnit.DAYS.between(request.getStartTime(), request.getCompletedTime())));
                        dto.setRequestStatus(RaiseRequestStatus.valueOf(request.getRequestStatus()));
                    }
                    userDetails.getAccountRetrievalRequests().add(dto);
                });
        saveCountDto.setAccRetrieveChangeActiveRequests(accRetrievalActiveRequestsCount);
        saveCountDto.setAccRetrieveChangeCompletedRequests(accRetrievalCompletedRequestsCount);
        saveCountDto.setAccRetrieveChangeDeclinedRequests(accRetrievalDeclinedRequestsCount);
    }

    private void functionCallToAddUserPasswordChangeHistory(UserProfileAndRequestDetailsDto userDetails) {
        userDetails.getPasswordChangeHistoryTrackDtoList()
                .addAll(
                        convertUserAuthHistInterfaceToDto(profileRepository.findTopByUserIdAndReasonTypeId(userDetails.getUserId(), reasonCodeIdAssociation.get(ReasonEnum.PASSWORD_CHANGE)))
                                .stream()
                                .map(responseHistory -> new PasswordChangeHistoryTrackDto(responseHistory.getComment(), responseHistory.getUpdatedTime()))
                                .toList()
                );
    }

    private void functionCallToAddUserForgotPasswordHistory(UserProfileAndRequestDetailsDto userDetails) {
        userDetails.getForgotPasswordHistoryTrackDtoList()
                .addAll(
                        convertUserAuthHistInterfaceToDto(profileRepository.findTopByUserIdAndReasonTypeId(userDetails.getUserId(), reasonCodeIdAssociation.get(ReasonEnum.FORGOT_PASSWORD)))
                                .stream()
                                .map(responseHistory -> new ForgotPasswordHistoryTrackDto(responseHistory.getComment(), responseHistory.getUpdatedTime()))
                                .toList()
                );
    }

    private void functionCallToAddUserRaisedRequestsHistory(List<ContactUs> allUserRequests, UserProfileAndRequestDetailsDto userDetails, Long adminUserId) {
        allUserRequests.stream()
                .filter(userRaisedDefect -> userRaisedDefect.getRequestReason().equalsIgnoreCase(RequestReason.USER_DEFECT_UPDATE.name()))
                .sorted((a, b) -> a.getStartTime().compareTo(b.getStartTime()))
                .forEach(userDefect -> {
                    UserDefectTrackingForAdminDto userDefectTrackingForAdminDto = new UserDefectTrackingForAdminDto();
                    userDefectTrackingForAdminDto.setStartTime(userDefect.getStartTime());
                    userDefectTrackingForAdminDto.setEndTime(userDefect.getCompletedTime());
                    userDefectTrackingForAdminDto.setReferenceNumber(userDefect.getReferenceNumber().startsWith("COM_") ? userDefect.getReferenceNumber().substring(4) : userDefect.getReferenceNumber());
                    userDefectTrackingForAdminDto.setStatus(userDefect.getRequestStatus());
                    userDefectTrackingForAdminDto.setDefectId(userDefect.getId());
                    List<ContactUsHist> userDefectHistoryDetails = contactUsHistRepository.findByContactUsId(userDefect.getId());
                    userDefectHistoryDetails.forEach(historyRecord -> {
                        if (historyRecord.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.SUBMITTED.name()))
                            userDefectTrackingForAdminDto.setDescription(historyRecord.getMessage());
                        if (historyRecord.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.PENDED.name())) {
                            userDefectTrackingForAdminDto.setPendTime(historyRecord.getUpdatedTime());
                            userDefectTrackingForAdminDto.setAdminRemarks(new StringBuilder("Pended For: " + historyRecord.getMessage() + " | "));
                            String username = Objects.requireNonNull(profileRepository.getUserAuthModelByUserId(historyRecord.getUpdatedBy()).orElse(null)).getUsername();
                            if (!Objects.equals(historyRecord.getUpdatedBy(), adminUserId)) userDefectTrackingForAdminDto.setRequestDoneBy(new StringBuilder("Pended By: " + username + " | "));
                            else userDefectTrackingForAdminDto.setRequestDoneBy(new StringBuilder("Pended By: You" + "(" + username + ") | "));
                        }
                        if (historyRecord.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.IGNORED.name()) || historyRecord.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.COMPLETED.name())) {
                            String username = Objects.requireNonNull(profileRepository.getUserAuthModelByUserId(historyRecord.getUpdatedBy()).orElse(null)).getUsername();
                            userDefectTrackingForAdminDto.getAdminRemarks().append(historyRecord.getRequestStatus()).append(" For: ").append(historyRecord.getMessage());
                            if (!Objects.equals(historyRecord.getUpdatedBy(), adminUserId)) userDefectTrackingForAdminDto.getRequestDoneBy().append(historyRecord.getRequestStatus().substring(0, 1).toUpperCase()).append(historyRecord.getRequestStatus().substring(1).toLowerCase()).append(" By: ").append(username);
                            else userDefectTrackingForAdminDto.getRequestDoneBy().append(historyRecord.getRequestStatus().substring(0,1).toUpperCase()).append(historyRecord.getRequestStatus().substring(1).toLowerCase()).append(" By: You" + "(").append(username).append(")");
                        }
                    });
                    userDetails.getUserDefectTrackingForAdminDtoList().add(userDefectTrackingForAdminDto);
                });
    }

    private void functionCallToAddGmailSyncCountIncreaseHistory( List<ContactUs> allUserRequests, Long adminUserId, UserProfileAndRequestDetailsDto userDetails) {
        allUserRequests.stream()
                .filter(gmailSyncRequest -> gmailSyncRequest.getRequestReason().equalsIgnoreCase(RequestReason.GMAIL_SYNC_REQUEST_TYPE.name()))
                .sorted((a, b) -> a.getStartTime().compareTo(b.getStartTime()))
                .forEach(gmailSyncRequest -> {
                    GmailSyncHistoryTrackDto gmailSyncHistoryTrackDto = new GmailSyncHistoryTrackDto();

                    LocalDateTime startTime = gmailSyncRequest.getStartTime();
                    LocalDateTime completedTime = gmailSyncRequest.getCompletedTime();
                    String requestStatus = gmailSyncRequest.getRequestStatus();

                    boolean isToday = startTime.toLocalDate().equals(LocalDate.now());

                    if (completedTime == null) {
                        if (isToday) gmailSyncHistoryTrackDto.setStatus("ACTIVE");
                        else if (RaiseRequestStatus.SUBMITTED.name().equalsIgnoreCase(requestStatus)) gmailSyncHistoryTrackDto.setStatus("EXPIRED");
                    } else {
                        if (RaiseRequestStatus.COMPLETED.name().equalsIgnoreCase(requestStatus)) gmailSyncHistoryTrackDto.setStatus("APPROVED");
                        else if (RaiseRequestStatus.CANCELLED.name().equalsIgnoreCase(requestStatus)) gmailSyncHistoryTrackDto.setStatus("REJECTED");

                        Duration duration = Duration.between(startTime, completedTime);
                        long hours = duration.toHours();
                        long minutes = duration.toMinutes() % 60;
                        long seconds = duration.getSeconds() % 60;

                        String totalTime = hours + " hrs " + minutes + " mins " + seconds + " secs";
                        gmailSyncHistoryTrackDto.setTotalTimeTaken(totalTime);
                    }
                    gmailSyncHistoryTrackDto.setStartDate(startTime);
                    gmailSyncHistoryTrackDto.setReferenceNumber(gmailSyncRequest.getReferenceNumber().startsWith("COM_") ? gmailSyncRequest.getReferenceNumber().substring(4) : gmailSyncRequest.getReferenceNumber());

                    List<ContactUsHist> userDefectHistoryDetails = contactUsHistRepository.findByContactUsId(gmailSyncRequest.getId());
                    userDefectHistoryDetails.forEach(historyRecord -> {
                        if (historyRecord.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.SUBMITTED.name())) {
                            try {
                                GmailSyncCountJsonDto jsonDataDto = objectMapper.readValue(historyRecord.getMessage(), GmailSyncCountJsonDto.class);
                                gmailSyncHistoryTrackDto.setRequestedReasonAndCount("Requested Count: " + jsonDataDto.getCount() + " | " + "Reason: " + jsonDataDto.getReason());
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        if (historyRecord.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.CANCELLED.name()) || historyRecord.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.COMPLETED.name())) {
                            gmailSyncHistoryTrackDto.setAdminRemarks(historyRecord.getMessage());
                            if (!Objects.equals(historyRecord.getUpdatedBy(), adminUserId)) gmailSyncHistoryTrackDto.setRequestDoneBy(Objects.requireNonNull(profileRepository.getUserAuthModelByUserId(historyRecord.getUpdatedBy()).orElse(null)).getUsername());
                            else gmailSyncHistoryTrackDto.setRequestDoneBy("You" + " (" + Objects.requireNonNull(profileRepository.getUserAuthModelByUserId(historyRecord.getUpdatedBy()).orElse(null)).getUsername() + ")");
                        }
                    });
                    gmailSyncHistoryTrackDto.setRequestId(gmailSyncRequest.getId());
                    userDetails.getUserGmailSyncCountIncreaseRequestHistoryTractDtoLit().add(gmailSyncHistoryTrackDto);
                });
    }

    private byte[] convertMultipartFileToPdfBytes(MultipartFile file){
        try {
            return file.getBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<UserAuthHist> convertUserAuthHistInterfaceToDto(List<UserAuthHistProjection> interfaceAuthHistList) {
        if (interfaceAuthHistList == null || interfaceAuthHistList.isEmpty()) {
            return new ArrayList<>();
        }
        return interfaceAuthHistList.stream()
                .map(projection -> {
                    UserAuthHist userAuthHist = new UserAuthHist();
                    userAuthHist.setId(projection.getId());
                    userAuthHist.setUserId(projection.getUserId());
                    userAuthHist.setUpdatedTime(projection.getUpdatedTime());
                    userAuthHist.setComment(projection.getComment());
                    userAuthHist.setUpdatedBy(projection.getUpdatedBy());
                    userAuthHist.setReasonTypeId(projection.getReasonTypeId());
                    return userAuthHist;
                })
                .toList();
    }
}
