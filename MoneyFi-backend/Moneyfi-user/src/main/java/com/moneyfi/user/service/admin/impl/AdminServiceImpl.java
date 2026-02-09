package com.moneyfi.user.service.admin.impl;

import com.moneyfi.user.exceptions.ResourceNotFoundException;
import com.moneyfi.user.exceptions.ScenarioNotPossibleException;
import com.moneyfi.user.model.*;
import com.moneyfi.user.model.dto.UserAuthHist;
import com.moneyfi.user.model.dto.UserAuthModel;
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
import com.moneyfi.user.service.common.UserCommonService;
import com.moneyfi.user.service.common.dto.response.UserFeedbackResponseDto;
import com.moneyfi.user.util.EmailTemplates;
import com.moneyfi.user.util.enums.*;
import com.moneyfi.user.validator.AdminValidations;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.moneyfi.user.util.constants.StringConstants.*;
import static com.moneyfi.user.util.constants.StringConstants.USER_NOT_FOUND;

@Service
@Slf4j
public class AdminServiceImpl implements AdminService {

    private static final String STATUS_REJECTED = "REJECTED";
    private static final String STATUS_APPROVED = "APPROVED";

    private final AdminRepository adminRepository;
    private final ContactUsRepository contactUsRepository;
    private final ProfileRepository profileRepository;
    private final ContactUsHistRepository contactUsHistRepository;
    private final ReasonDetailsRepository reasonDetailsRepository;
    private final EmailTemplates emailTemplates;
    private final CommonServiceRepository commonServiceRepository;
    private final ScheduleNotificationRepository scheduleNotificationRepository;
    private final UserCommonService userCommonService;
    private final UserNotificationRepository userNotificationRepository;

    public AdminServiceImpl(AdminRepository adminRepository,
                            ContactUsRepository contactUsRepository,
                            ProfileRepository profileRepository,
                            ContactUsHistRepository contactUsHistRepository,
                            ReasonDetailsRepository reasonDetailsRepository,
                            EmailTemplates emailTemplates,
                            CommonServiceRepository commonServiceRepository,
                            ScheduleNotificationRepository scheduleNotificationRepository,
                            UserCommonService userCommonService,
                            UserNotificationRepository userNotificationRepository){
        this.adminRepository = adminRepository;
        this.contactUsRepository = contactUsRepository;
        this.profileRepository = profileRepository;
        this.contactUsHistRepository = contactUsHistRepository;
        this.reasonDetailsRepository = reasonDetailsRepository;
        this.emailTemplates = emailTemplates;
        this.commonServiceRepository = commonServiceRepository;
        this.scheduleNotificationRepository = scheduleNotificationRepository;
        this.userCommonService = userCommonService;
        this.userNotificationRepository = userNotificationRepository;
    }

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
    public boolean accountReactivationAndNameChangeRequest(String email, String referenceNumber, String requestStatus, Long adminUserId, String approveStatus, String declineReason) {
        return contactUsRepository.findByEmail(email)
                .stream()
                .filter(ContactUs::isRequestActive)
                .filter(i -> i.getReferenceNumber() != null &&
                        i.getReferenceNumber().trim().equalsIgnoreCase(referenceNumber.trim()))
                .findFirst()
                .map(request -> {
                    if(approveStatus.equalsIgnoreCase(ApproveStatus.APPROVE.name()))
                    functionCallToChangeDetails(email, request, requestStatus, adminUserId);
                    else if(approveStatus.equalsIgnoreCase(ApproveStatus.DECLINE.name())) functionCallToDeclineTheUserRequest(request, declineReason);
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
    public void updateDefectStatus(Long defectId, String status, String reason) {
        ContactUs userDefect = contactUsRepository.findById(defectId).orElseThrow(() -> new ResourceNotFoundException("User defect details not found"));
        Optional<ProfileModel> userProfile = profileRepository.findByUserEmail(userDefect.getEmail());
        if(userDefect.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.COMPLETED.name()) ||
                userDefect.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.IGNORED.name()) ||
                userDefect.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.CANCELLED.name())){
            throw new ScenarioNotPossibleException("Action already done");
        }
        ContactUsHist userDefectHist = new ContactUsHist();
        if(status.equalsIgnoreCase("Solved")){
            userDefect.setRequestStatus(RaiseRequestStatus.COMPLETED.name());
            userDefect.setRequestActive(false);
            userDefect.setReferenceNumber("COM_" + userDefect.getReferenceNumber());
            userDefect.setCompletedTime(LocalDateTime.now());
            userDefect.setVerified(true);

            userDefectHist.setMessage("Development team completed, Admin has been approved");
            userDefectHist.setContactUsId(userDefect.getId());
            userDefectHist.setRequestReason(RequestReason.USER_DEFECT_UPDATE.name());
            userDefectHist.setRequestStatus(RaiseRequestStatus.COMPLETED.name());
            userDefectHist.setUpdatedTime(userDefect.getCompletedTime());
            new Thread(() -> emailTemplates.sendUserReportStatusMailToUser(userProfile.get().getName(), userDefect.getReferenceNumber().substring(4), userDefectHist.getMessage(), userDefect.getEmail().trim())).start();
        } else if(status.equalsIgnoreCase("Pend")){
            userDefect.setRequestStatus(RaiseRequestStatus.PENDED.name());

            userDefectHist.setMessage("Admin kept in Pended state. Need some accuracy");
            userDefectHist.setContactUsId(userDefect.getId());
            userDefectHist.setRequestReason(RequestReason.USER_DEFECT_UPDATE.name());
            userDefectHist.setRequestStatus(RaiseRequestStatus.PENDED.name());
            userDefectHist.setUpdatedTime(LocalDateTime.now());
        } else if (status.equalsIgnoreCase("Ignore")){
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
            new Thread(() -> emailTemplates.sendUserReportStatusMailToUser(userProfile.get().getName(), userDefect.getReferenceNumber().substring(4), userDefectHist.getMessage(), userDefect.getEmail().trim())).start();
        }
        contactUsRepository.save(userDefect);
        contactUsHistRepository.save(userDefectHist);
    }

    @Override
    public Map<Integer, Integer> getUserMonthlyCountInAYear(int year, String status) {
        return adminRepository.getUserMonthlyCountInAYear(year, status);
    }

    @Override
    public UserProfileAndRequestDetailsDto getCompleteUserDetailsForAdmin(String username) {
        UserProfileAndRequestDetailsDto userDetails = adminRepository.getCompleteUserDetailsForAdmin(username);
        List<ContactUs> allUserRequests = contactUsRepository.findByEmail(username);
        AdminUserRequestsCountDto saveCountDto = new AdminUserRequestsCountDto();

//        new Thread(
//                () -> userDetails.setImageFromS3(awsServices.fetchUserProfilePictureFromS3(userDetails.getUserId(), username))
//        ).start();
//        userDetails.setProfileImage(userService.fetchUserProfilePictureFromS3(username))

        addNameRequestDetailsToUserDetails(userDetails, allUserRequests, saveCountDto);
        addUnblockRequestDetailsToUserDetails(userDetails, allUserRequests, saveCountDto);
        addAccRetrievalRequestDetailsToUserDetails(userDetails, allUserRequests, saveCountDto);

        userDetails.getPasswordChangeHistoryTrackDtoList()
                .addAll(
                        convertUserAuthHistInterfaceToDto(profileRepository.findTopByUserIdAndReasonTypeId(userDetails.getUserId(), reasonCodeIdAssociation.get(ReasonEnum.PASSWORD_CHANGE)))
                                .stream()
                                .map(responseHistory -> new PasswordChangeHistoryTrackDto(responseHistory.getComment(), responseHistory.getUpdatedTime()))
                                .toList()
                );
        userDetails.getForgotPasswordHistoryTrackDtoList()
                .addAll(
                        convertUserAuthHistInterfaceToDto(profileRepository.findTopByUserIdAndReasonTypeId(userDetails.getUserId(), reasonCodeIdAssociation.get(ReasonEnum.FORGOT_PASSWORD)))
                                .stream()
                                .map(responseHistory -> new ForgotPasswordHistoryTrackDto(responseHistory.getComment(), responseHistory.getUpdatedTime()))
                                .toList()
                );

        allUserRequests
                .stream()
                .filter(nameChangeRequest -> nameChangeRequest.getRequestReason().equalsIgnoreCase(RequestReason.USER_DEFECT_UPDATE.name()))
                .sorted((a, b) -> a.getStartTime().compareTo(b.getStartTime()))
                .forEach(userDefect -> {
                    userDetails.getUserDefectTrackingForAdminDtoList().add(
                            new UserDefectTrackingForAdminDto(userDefect.getStartTime(), userDefect.getCompletedTime(),
                                    (userDefect.getReferenceNumber().startsWith("COM_") ? userDefect.getReferenceNumber().substring(4) : userDefect.getReferenceNumber()),
                                            userDefect.getRequestStatus(), userDefect.getId()));
                });
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
    public void updateUserFeedback(Long feedbackId) {
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
        contactUsHistRepository.save(userFeedbackHist);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void addReasonsForUserReasonDialog(ReasonDetailsRequestDto requestDto) {
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
    public void updateReasonsForUserReasonDialogByReasonCode(ReasonUpdateRequestDto requestDto) {
        if(requestDto.getReason() == null || requestDto.getReason().isEmpty()){
            throw new ScenarioNotPossibleException("Please add details correctly");
        }
        ReasonDetails reasonDetails = reasonDetailsRepository.findById(requestDto.getReasonId())
                .orElseThrow(() -> new ResourceNotFoundException("Reason with id " + requestDto.getReasonId() + " is not found"));
        reasonDetails.setReason(requestDto.getReason());
        reasonDetails.setUpdatedTime(LocalDateTime.now());
        reasonDetailsRepository.save(reasonDetails);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void deleteReasonByReasonId(int reasonId) {
        ReasonDetails reasonDetails = reasonDetailsRepository.findById(reasonId)
                .orElseThrow(() -> new ResourceNotFoundException("Reason with id " + reasonId + " is not found"));
        reasonDetails.setIsDeleted(true);
        reasonDetails.setUpdatedTime(LocalDateTime.now());
        reasonDetailsRepository.save(reasonDetails);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public String blockTheUserAccountByAdmin(String email, String reason, MultipartFile file, Long adminUserId) {
        if(email == null || email.trim().isEmpty() || reason == null || reason.trim().isEmpty()){
            throw new ScenarioNotPossibleException("Please provide all the details correctly");
        }
        UserAuthModel user = convertUserAuthInterfaceToDto(profileRepository.getUserDetailsByUsername(email).orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND)));
        if (user.isBlocked()) {
            throw new ScenarioNotPossibleException("User account is already blocked");
        }
        if(user.isDeleted()){
            throw new ScenarioNotPossibleException("User account is deleted, can't block the user");
        }
        profileRepository.updateUserAuthTableWithBlockOrUnblockStatus(user.getId(), false);

        ProfileModel userProfile = profileRepository.findByUserId(user.getId()).orElseThrow(() -> new ResourceNotFoundException(USER_PROFILE_NOT_FOUND));
        ContactUs contactUs = new ContactUs();
        contactUs.setEmail(email);
        contactUs.setRequestReason(RequestReason.ACCOUNT_BLOCK_REQUEST.name());
        contactUs.setRequestActive(true);
        contactUs.setVerified(false);
        contactUs.setRequestStatus(RaiseRequestStatus.INITIATED.name());
        contactUs.setStartTime(LocalDateTime.now());
        String referenceNumber = "BL" + userProfile.getName().substring(0,2) + email.substring(0,2)
                + (userProfile.getPhone() != null ? userProfile.getPhone().substring(0,2) + generateVerificationCode().substring(0,3) : generateVerificationCode());
        contactUs.setReferenceNumber(referenceNumber);
        ContactUs savedContactUs = contactUsRepository.save(contactUs);

        ContactUsHist contactUsHist = new ContactUsHist();
        contactUsHist.setContactUsId(savedContactUs.getId());
        contactUsHist.setName(userProfile.getName());
        contactUsHist.setMessage(BLOCKED_BY_ADMIN + ", " + reason);
        contactUsHist.setRequestReason(RequestReason.ACCOUNT_BLOCK_REQUEST.name());
        contactUsHist.setRequestStatus(RaiseRequestStatus.INITIATED.name());
        contactUsHist.setUpdatedTime(savedContactUs.getStartTime());
        contactUsHistRepository.save(contactUsHist);
        methodToUpdateUserAuthHistTable(user.getId(), reasonCodeIdAssociation.get(ReasonEnum.BLOCK_ACCOUNT), reason, adminUserId, LocalDateTime.now());
        new Thread(
                () -> emailTemplates.sendBlockAlertMailToUser(email, reason, profileRepository.findByUserId(user.getId()).get().getName(), convertMultipartFileToPdfBytes(file))
        ).start();
        return "User is successfully blocked";
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
    public void scheduleNotification(ScheduleNotificationRequestDto requestDto) {
        AdminValidations.validateScheduleNotificationRequestDetails(requestDto);
        ScheduleNotification scheduleNotification = new ScheduleNotification();
        BeanUtils.copyProperties(requestDto, scheduleNotification);
        ScheduleNotification response = scheduleNotificationRepository.save(scheduleNotification);
        functionToSaveNotificationToUsers(requestDto.getRecipients(), response.getId());
    }

    @Override
    public List<AdminSchedulesResponseDto> getAllActiveSchedulesOfAdmin(String status) {
        return adminRepository.getAllActiveSchedulesOfAdmin(status)
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
    public void cancelTheUserScheduling(Long scheduleId) {
        ScheduleNotification notification = scheduleNotificationRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + scheduleId));
        if (Boolean.TRUE.equals(notification.isCancelled())) {
            throw new IllegalStateException("Schedule with id " + scheduleId + " is already cancelled.");
        }
        notification.setCancelled(true);
        notification.setUpdatedAt(LocalDateTime.now());
        scheduleNotificationRepository.save(notification);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void updateAdminPlacedSchedules(AdminScheduleRequestDto requestDto) {
        ScheduleNotification notification = scheduleNotificationRepository.findById(requestDto.getScheduleId())
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + requestDto.getScheduleId()));
        AdminValidations.validateScheduleNotificationRequestDetails(new ScheduleNotificationRequestDto(requestDto.getSubject(), requestDto.getDescription(), requestDto.getScheduleFrom(), requestDto.getScheduleTo(), requestDto.getRecipients()));

        notification.setSubject(requestDto.getSubject());
        notification.setDescription(requestDto.getDescription());
        notification.setScheduleFrom(requestDto.getScheduleFrom());
        notification.setScheduleTo(requestDto.getScheduleTo());
        notification.setRecipients(requestDto.getRecipients());
        notification.setDescription(notification.getDescription());
        notification.setUpdatedAt(LocalDateTime.now());
        notification.setCancelled(false);
        notification.setActive(true);
        scheduleNotificationRepository.save(notification);
        functionToSaveNotificationToUsers(requestDto.getRecipients(), requestDto.getScheduleId());
        new Thread(() -> userNotificationRepository.deleteAllByScheduleId(requestDto.getScheduleId())).start();
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void deleteUserScheduling(Long scheduleId) {
        ScheduleNotification notification = scheduleNotificationRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + scheduleId));
        notification.setActive(false);
        notification.setUpdatedAt(LocalDateTime.now());
        scheduleNotificationRepository.save(notification);
        new Thread(() -> userNotificationRepository.deleteAllByScheduleId(scheduleId)).start();
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
            // Create Header Row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"S No", "Name", "Username", "Phone", "Created Time", "Date of Birth"};
            for(int i=0; i< headers.length; i++){
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(createHeaderStyle(workbook));
            }
            // Create a Date Style
            CellStyle dateStyle = createDateStyle(workbook);
            // Populate Data Rows
            int rowIndex = 1;
            for (UserGridDto data : userGridDtoList) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(data.getSlNo());
                row.createCell(1).setCellValue(data.getName());
                row.createCell(2).setCellValue(data.getUsername());
                row.createCell(3).setCellValue(data.getPhone()!=null?data.getPhone():"-");
                // Format Date Properly
                Cell dateCell = row.createCell(4);
                dateCell.setCellValue(data.getCreatedDateTime()); // Assuming data.getDate() is `java.util.Date`
                dateCell.setCellStyle(dateStyle); // Apply formatting

                Cell dateCell2 = row.createCell(5);
                dateCell2.setCellValue(data.getDateOfBirth()); // Assuming data.getDate() is `java.util.Date`
                dateCell2.setCellStyle(dateStyle); // Apply formatting
            }
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            // Convert to byte array
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
        // Set Background Color
        style.setFillForegroundColor(IndexedColors.YELLOW.getIndex()); // Yellow background
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND); // Apply solid fill
        // Set Border (Optional)
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private void functionCallToChangeDetails(String email, ContactUs contactUs, String requestStatus, Long adminUserId){
        UserAuthModel user = convertUserAuthInterfaceToDto(profileRepository.getUserDetailsByUsername(email).orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND)));
        ContactUsHist requestUserHist = new ContactUsHist();

        if(requestStatus.equalsIgnoreCase(RequestReason.ACCOUNT_UNBLOCK_REQUEST.name())){
            profileRepository.updateUserAuthTableWithBlockOrUnblockStatus(user.getId(), false);
            ContactUsHist requestDetailsHist = contactUsHistRepository.findByContactUsIdList(contactUs.getId())
                    .stream()
                    .filter(request -> request.getRequestReason().equalsIgnoreCase(RequestReason.ACCOUNT_UNBLOCK_REQUEST.name()))
                    .findFirst()
                    .get();
            LocalDateTime completedTime = LocalDateTime.now();
            methodToUpdateUserAuthHistTable(user.getId(), reasonCodeIdAssociation.get(ReasonEnum.UNBLOCK_ACCOUNT), requestDetailsHist.getMessage(), adminUserId, completedTime);
            requestUserHist.setRequestReason(RequestReason.ACCOUNT_UNBLOCK_REQUEST.name());
            methodToUpdateContactUsTable(contactUs, requestUserHist, completedTime);
        } else if (requestStatus.equalsIgnoreCase(RequestReason.ACCOUNT_NOT_DELETE_REQUEST.name())){
            profileRepository.updateUserAuthTableWithDeleteOrUndeleteStatus(user.getId(), false);
            ContactUsHist requestDetailsHist = contactUsHistRepository.findByContactUsIdList(contactUs.getId())
                    .stream()
                    .filter(request -> request.getRequestReason().equalsIgnoreCase(RequestReason.ACCOUNT_NOT_DELETE_REQUEST.name()))
                    .findFirst()
                    .get();
            LocalDateTime completedTime = LocalDateTime.now();
            methodToUpdateUserAuthHistTable(user.getId(), reasonCodeIdAssociation.get(ReasonEnum.ACCOUNT_RETRIEVAL), requestDetailsHist.getMessage(), adminUserId, completedTime);
            requestUserHist.setRequestReason(RequestReason.ACCOUNT_NOT_DELETE_REQUEST.name());
            methodToUpdateContactUsTable(contactUs, requestUserHist, completedTime);
        } else if (requestStatus.equalsIgnoreCase(RequestReason.NAME_CHANGE_REQUEST.name())){
            ProfileModel userProfile = profileRepository.findByUserId(user.getId()).orElseThrow(() -> new ResourceNotFoundException("User profile not found"));
            ContactUsHist requestDetailsHist = contactUsHistRepository.findByContactUsIdList(contactUs.getId())
                    .stream()
                    .findFirst()
                    .get();
            if(!userProfile.getName().toLowerCase().contains(requestDetailsHist.getMessage().toLowerCase().split(",")[0])){
                throw new ScenarioNotPossibleException("Old name didn't match");
            }
            LocalDateTime completedTime = LocalDateTime.now();
            userProfile.setName(requestDetailsHist.getName());
            profileRepository.save(userProfile);
            methodToUpdateUserAuthHistTable(user.getId(), reasonCodeIdAssociation.get(ReasonEnum.NAME_CHANGE), requestDetailsHist.getMessage().split(",")[1], adminUserId, completedTime);
            requestUserHist.setRequestReason(RequestReason.NAME_CHANGE_REQUEST.name());
            methodToUpdateContactUsTable(contactUs, requestUserHist, completedTime);
        }
    }

    private void methodToUpdateContactUsTable(ContactUs contactUs, ContactUsHist requestUserHist, LocalDateTime completedTime){
        contactUs.setRequestActive(false);
        contactUs.setVerified(true);
        contactUs.setReferenceNumber("COM_" + contactUs.getReferenceNumber());
        contactUs.setRequestStatus(RaiseRequestStatus.COMPLETED.name());
        contactUs.setCompletedTime(completedTime);
        ContactUs savedRequest = contactUsRepository.save(contactUs);

        requestUserHist.setContactUsId(savedRequest.getId());
        requestUserHist.setRequestStatus(RaiseRequestStatus.COMPLETED.name());
        requestUserHist.setUpdatedTime(completedTime);
        contactUsHistRepository.save(requestUserHist);
    }

    private void methodToUpdateUserAuthHistTable(Long userId, int reasonTypeId, String comment, Long updatedUserId, LocalDateTime completedTime){
        profileRepository.insertUserAuthHistory(userId, completedTime, reasonTypeId, comment, updatedUserId);
    }

    private void functionCallToDeclineTheUserRequest(ContactUs contactUs, String declineReason){
        if(declineReason == null || declineReason.trim().isEmpty()){
            throw new ScenarioNotPossibleException("Decline reason should not be empty");
        }
        contactUs.setCompletedTime(LocalDateTime.now());
        contactUs.setRequestActive(false);
        contactUs.setVerified(true);
        contactUs.setReferenceNumber("COM_" + contactUs.getReferenceNumber());
        contactUs.setRequestStatus(RaiseRequestStatus.CANCELLED.name());
        ContactUs response = contactUsRepository.save(contactUs);

        ContactUsHist contactUsHist = new ContactUsHist();
        contactUsHist.setContactUsId(response.getId());
        contactUsHist.setMessage(declineReason);
        contactUsHist.setRequestReason(response.getRequestReason());
        contactUsHist.setRequestStatus(response.getRequestStatus());
        contactUsHist.setUpdatedTime(response.getCompletedTime());
        contactUsHistRepository.save(contactUsHist);
    }

    private void addNameRequestDetailsToUserDetails(UserProfileAndRequestDetailsDto userDetails, List<ContactUs> allUserRequests, AdminUserRequestsCountDto saveCountDto){
        AtomicInteger nameChangeActiveRequestsCount = new AtomicInteger(0);
        AtomicInteger nameChangeCompletedRequestsCount = new AtomicInteger(0);
        AtomicInteger nameChangeDeclinedRequestsCount = new AtomicInteger(0);
        allUserRequests
                .stream()
                .filter(nameChangeRequest -> nameChangeRequest.getRequestReason().equalsIgnoreCase(RequestReason.NAME_CHANGE_REQUEST.name()))
                .sorted((a, b) -> a.getStartTime().compareTo(b.getStartTime()))
                .forEach(request -> {
                    if(request.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.SUBMITTED.name()) || request.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.INITIATED.name())){
                        nameChangeActiveRequestsCount.getAndIncrement();
                    } else if (request.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.COMPLETED.name())){
                        nameChangeCompletedRequestsCount.getAndIncrement();
                    } else if(request.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.CANCELLED.name())){
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
                        if (nameChangeRequestHist.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.COMPLETED.name()) ||
                                nameChangeRequestHist.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.CANCELLED.name())) {
                            requestTimeStatusHistoryMap.put(RaiseRequestStatus.COMPLETED, new UserRequestsUpdatedHistDto(Timestamp.valueOf(nameChangeRequestHist.getUpdatedTime())));
                            if (nameChangeRequestHist.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.CANCELLED.name()))
                                approvedOrRejectedMap.put(STATUS_REJECTED, nameChangeRequestHist.getMessage());
                            else approvedOrRejectedMap.put(STATUS_APPROVED, nameChangeRequestHist.getMessage());
                        }
                        dto.setApprovedOrRejected(approvedOrRejectedMap);
                        dto.setRequestTimeStatusHistory(requestTimeStatusHistoryMap);
                    });
                    dto.setReferenceNumber(request.getReferenceNumber().startsWith("COM_") ? request.getReferenceNumber().substring(4) : request.getReferenceNumber());
                    if (request.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.SUBMITTED.name()) || request.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.INITIATED.name())) {
                        dto.setRequestStatus(RaiseRequestStatus.valueOf(request.getRequestStatus()));
                        dto.setDaysTakenForCompletion(null);
                    } else {
                        dto.setDaysTakenForCompletion((int) ChronoUnit.DAYS.between(request.getStartTime(), request.getCompletedTime()));
                        dto.setRequestStatus(RaiseRequestStatus.valueOf(request.getRequestStatus()));
                    }
                    userDetails.getNameChangeRequests().add(dto);
                });
        saveCountDto.setNameChangeActiveRequests(nameChangeActiveRequestsCount);
        saveCountDto.setNameChangeCompletedRequests(nameChangeCompletedRequestsCount);
        saveCountDto.setNameChangeDeclinedRequests(nameChangeDeclinedRequestsCount);
    }

    private void addUnblockRequestDetailsToUserDetails(UserProfileAndRequestDetailsDto userDetails, List<ContactUs> allUserRequests, AdminUserRequestsCountDto saveCountDto){
        AtomicInteger accBlockActiveRequestsCount = new AtomicInteger(0);
        AtomicInteger accBlockCompletedRequestsCount = new AtomicInteger(0);
        AtomicInteger accBlockDeclinedRequestsCount = new AtomicInteger(0);
        allUserRequests
                .stream()
                .filter(accUnblockRequest -> accUnblockRequest.getRequestReason().equalsIgnoreCase(RequestReason.ACCOUNT_UNBLOCK_REQUEST.name()))
                .sorted((a, b) -> a.getStartTime().compareTo(b.getStartTime()))
                .forEach(request -> {
                    if(request.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.SUBMITTED.name()) || request.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.INITIATED.name())){
                        accBlockActiveRequestsCount.getAndIncrement();
                    } else if (request.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.COMPLETED.name())){
                        accBlockCompletedRequestsCount.getAndIncrement();
                    } else if(request.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.CANCELLED.name())){
                        accBlockDeclinedRequestsCount.getAndIncrement();
                    }
                    AdminUserUnblockRequestDetailsDto dto = new AdminUserUnblockRequestDetailsDto();
                    Map<RaiseRequestStatus, UserRequestsUpdatedHistDto> requestTimeStatusHistoryMap = new HashMap<>();
                    Map<String, String> approvedOrRejectedMap = new HashMap<>();
                    List<ContactUsHist> unblockAccHistList = contactUsHistRepository.findByContactUsId(request.getId());
                    unblockAccHistList.forEach(accUnblockHistRequest -> {
                        if(accUnblockHistRequest.getRequestReason().equalsIgnoreCase(RequestReason.ACCOUNT_BLOCK_REQUEST.name())){
                            if(accUnblockHistRequest.getMessage().split(",")[0].equalsIgnoreCase(BLOCKED_BY_USER))
                                dto.setBlockedBy("USER");
                            else dto.setBlockedBy("ADMIN");
                        } else {
                            if (accUnblockHistRequest.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.INITIATED.name())) {
                                requestTimeStatusHistoryMap.put(RaiseRequestStatus.INITIATED, new UserRequestsUpdatedHistDto(Timestamp.valueOf(accUnblockHistRequest.getUpdatedTime())));
                            }
                            if (accUnblockHistRequest.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.SUBMITTED.name())) {
                                dto.setUnblockRequestReason(accUnblockHistRequest.getMessage());
                                requestTimeStatusHistoryMap.put(RaiseRequestStatus.SUBMITTED, new UserRequestsUpdatedHistDto(Timestamp.valueOf(accUnblockHistRequest.getUpdatedTime())));
                            }
                            if (accUnblockHistRequest.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.COMPLETED.name()) ||
                                    accUnblockHistRequest.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.CANCELLED.name())) {
                                requestTimeStatusHistoryMap.put(RaiseRequestStatus.COMPLETED, new UserRequestsUpdatedHistDto(Timestamp.valueOf(accUnblockHistRequest.getUpdatedTime())));
                                if (accUnblockHistRequest.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.CANCELLED.name()))
                                    approvedOrRejectedMap.put(STATUS_REJECTED, accUnblockHistRequest.getMessage());
                                else approvedOrRejectedMap.put(STATUS_APPROVED, accUnblockHistRequest.getMessage());
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
                        dto.setDaysTakenForCompletion((int) ChronoUnit.DAYS.between(request.getStartTime(), request.getCompletedTime()));
                        dto.setRequestStatus(RaiseRequestStatus.valueOf(request.getRequestStatus()));
                    }
                    userDetails.getUnblockAccountRequests().add(dto);
                });
        saveCountDto.setAccBlockActiveRequests(accBlockActiveRequestsCount);
        saveCountDto.setAccBlockChangeCompletedRequests(accBlockCompletedRequestsCount);
        saveCountDto.setAccBlockChangeDeclinedRequests(accBlockDeclinedRequestsCount);
    }

    private void addAccRetrievalRequestDetailsToUserDetails(UserProfileAndRequestDetailsDto userDetails, List<ContactUs> allUserRequests, AdminUserRequestsCountDto saveCountDto){
        AtomicInteger accRetrievalActiveRequestsCount = new AtomicInteger(0);
        AtomicInteger accRetrievalCompletedRequestsCount = new AtomicInteger(0);
        AtomicInteger accRetrievalDeclinedRequestsCount = new AtomicInteger(0);
        allUserRequests
                .stream()
                .filter(accUnblockRequest -> accUnblockRequest.getRequestReason().equalsIgnoreCase(RequestReason.ACCOUNT_NOT_DELETE_REQUEST.name()))
                .sorted((a, b) -> a.getStartTime().compareTo(b.getStartTime()))
                .forEach(request -> {
                    if(request.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.SUBMITTED.name()) || request.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.INITIATED.name())){
                        accRetrievalActiveRequestsCount.getAndIncrement();
                    } else if (request.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.COMPLETED.name())){
                        accRetrievalCompletedRequestsCount.getAndIncrement();
                    } else if(request.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.CANCELLED.name())){
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
                        if (accRetrievalHistRequest.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.COMPLETED.name()) ||
                                accRetrievalHistRequest.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.CANCELLED.name())) {
                            requestTimeStatusHistoryMap.put(RaiseRequestStatus.COMPLETED, new UserRequestsUpdatedHistDto(Timestamp.valueOf(accRetrievalHistRequest.getUpdatedTime())));
                            if (accRetrievalHistRequest.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.CANCELLED.name()))
                                approvedOrRejectedMap.put(STATUS_REJECTED, accRetrievalHistRequest.getMessage());
                            else approvedOrRejectedMap.put(STATUS_APPROVED, accRetrievalHistRequest.getMessage());
                        }
                        dto.setApprovedOrRejected(approvedOrRejectedMap);
                        dto.setRequestTimeStatusHistory(requestTimeStatusHistoryMap);
                    });
                    dto.setReferenceNumber(request.getReferenceNumber().startsWith("COM_") ? request.getReferenceNumber().substring(4) : request.getReferenceNumber());
                    if (request.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.SUBMITTED.name()) || request.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.INITIATED.name())) {
                        dto.setRequestStatus(RaiseRequestStatus.valueOf(request.getRequestStatus()));
                        dto.setDaysTakenForCompletion(null);
                    } else {
                        dto.setDaysTakenForCompletion((int) ChronoUnit.DAYS.between(request.getStartTime(), request.getCompletedTime()));
                        dto.setRequestStatus(RaiseRequestStatus.valueOf(request.getRequestStatus()));
                    }
                    userDetails.getAccountRetrievalRequests().add(dto);
                });
        saveCountDto.setAccRetrieveChangeActiveRequests(accRetrievalActiveRequestsCount);
        saveCountDto.setAccRetrieveChangeCompletedRequests(accRetrievalCompletedRequestsCount);
        saveCountDto.setAccRetrieveChangeDeclinedRequests(accRetrievalDeclinedRequestsCount);
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
