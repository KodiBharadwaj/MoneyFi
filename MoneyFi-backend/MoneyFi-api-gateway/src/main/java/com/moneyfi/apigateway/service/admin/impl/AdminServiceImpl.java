package com.moneyfi.apigateway.service.admin.impl;

import com.moneyfi.apigateway.exceptions.ResourceNotFoundException;
import com.moneyfi.apigateway.exceptions.ScenarioNotPossibleException;
import com.moneyfi.apigateway.model.auth.UserAuthModel;
import com.moneyfi.apigateway.model.common.*;
import com.moneyfi.apigateway.repository.admin.AdminRepository;
import com.moneyfi.apigateway.repository.user.*;
import com.moneyfi.apigateway.repository.user.auth.UserRepository;
import com.moneyfi.apigateway.service.admin.AdminService;
import com.moneyfi.apigateway.service.admin.dto.request.AdminScheduleRequestDto;
import com.moneyfi.apigateway.service.admin.dto.request.ReasonDetailsRequestDto;
import com.moneyfi.apigateway.service.admin.dto.request.ReasonUpdateRequestDto;
import com.moneyfi.apigateway.service.admin.dto.request.ScheduleNotificationRequestDto;
import com.moneyfi.apigateway.service.admin.dto.response.*;
import com.moneyfi.apigateway.service.common.AwsServices;
import com.moneyfi.apigateway.service.common.dto.response.UserFeedbackResponseDto;
import com.moneyfi.apigateway.util.enums.*;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.moneyfi.apigateway.util.constants.StringUtils.reasonCodeIdAssociation;
import static com.moneyfi.apigateway.util.constants.StringUtils.userRoleAssociation;

@Service
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final ContactUsRepository contactUsRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ContactUsHistRepository contactUsHistRepository;
    private final ScheduleNotificationRepository scheduleNotificationRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final AwsServices awsServices;
    private final ReasonDetailsRepository reasonDetailsRepository;
    private final UserAuthHistRepository userAuthHistRepository;

    public AdminServiceImpl(AdminRepository adminRepository,
                            ContactUsRepository contactUsRepository,
                            UserRepository userRepository,
                            ProfileRepository profileRepository,
                            ContactUsHistRepository contactUsHistRepository,
                            ScheduleNotificationRepository scheduleNotificationRepository,
                            UserNotificationRepository userNotificationRepository,
                            AwsServices awsServices,
                            ReasonDetailsRepository reasonDetailsRepository,
                            UserAuthHistRepository userAuthHistRepository){
        this.adminRepository = adminRepository;
        this.contactUsRepository = contactUsRepository;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.contactUsHistRepository = contactUsHistRepository;
        this.scheduleNotificationRepository = scheduleNotificationRepository;
        this.userNotificationRepository = userNotificationRepository;
        this.awsServices = awsServices;
        this.reasonDetailsRepository = reasonDetailsRepository;
        this.userAuthHistRepository = userAuthHistRepository;
    }

    @Override
    public AdminOverviewPageDto getAdminOverviewPageDetails() {
        AdminOverviewPageDto overviewPageDetails = adminRepository.getAdminOverviewPageDetails();
        overviewPageDetails.setTotalUsers(overviewPageDetails.getActiveUsers() + overviewPageDetails.getBlockedUsers() + overviewPageDetails.getDeletedUsers());
        return overviewPageDetails;
    }

    @Override
    public List<UserGridDto> getUserDetailsGridForAdmin(String status) {
        List<UserGridDto> userGridDtoList = adminRepository.getUserDetailsGridForAdmin(status);
        AtomicInteger i = new AtomicInteger(1);
        userGridDtoList.forEach(user -> user.setSlNo(i.getAndIncrement()));
        return userGridDtoList;
    }

    @Override
    public byte[] getUserDetailsExcelForAdmin(String status) {
        List<UserGridDto> userGridDtoList = getUserDetailsGridForAdmin(status);
        if(userGridDtoList.isEmpty()){
            throw new ResourceNotFoundException("No user data found to generate excel");
        }
        return generateExcelReport(userGridDtoList);
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

    @Override
    @Transactional
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

    private void functionCallToChangeDetails(String email, ContactUs contactUs, String requestStatus, Long adminUserId){
        UserAuthModel user = userRepository.getUserDetailsByUsername(email);
        ContactUsHist requestUserHist = new ContactUsHist();

        if(requestStatus.equalsIgnoreCase(RequestReason.ACCOUNT_UNBLOCK_REQUEST.name())){
            user.setBlocked(false);
            userRepository.save(user);
            ContactUsHist requestDetailsHist = contactUsHistRepository.findByContactUsIdList(contactUs.getId())
                    .stream()
                    .filter(request -> request.getRequestReason().equalsIgnoreCase(RequestReason.ACCOUNT_UNBLOCK_REQUEST.name()))
                    .findFirst()
                    .get();
            methodToUpdateUserAuthHistTable(user.getId(), reasonCodeIdAssociation.get(ReasonEnum.UNBLOCK_ACCOUNT), requestDetailsHist.getMessage(), adminUserId);
            requestUserHist.setRequestReason(RequestReason.ACCOUNT_UNBLOCK_REQUEST.name());
            methodToUpdateContactUsTable(contactUs, requestUserHist);
        } else if (requestStatus.equalsIgnoreCase(RequestReason.ACCOUNT_NOT_DELETE_REQUEST.name())){
            user.setDeleted(false);
            userRepository.save(user);
            ContactUsHist requestDetailsHist = contactUsHistRepository.findByContactUsIdList(contactUs.getId())
                    .stream()
                    .filter(request -> request.getRequestReason().equalsIgnoreCase(RequestReason.ACCOUNT_NOT_DELETE_REQUEST.name()))
                    .findFirst()
                    .get();
            methodToUpdateUserAuthHistTable(user.getId(), reasonCodeIdAssociation.get(ReasonEnum.ACCOUNT_RETRIEVAL), requestDetailsHist.getMessage(), adminUserId);
            requestUserHist.setRequestReason(RequestReason.ACCOUNT_NOT_DELETE_REQUEST.name());
            methodToUpdateContactUsTable(contactUs, requestUserHist);
        } else if (requestStatus.equalsIgnoreCase(RequestReason.NAME_CHANGE_REQUEST.name())){
            ProfileModel userProfile = profileRepository.findByUserId(user.getId());
            ContactUsHist requestDetailsHist = contactUsHistRepository.findByContactUsIdList(contactUs.getId())
                    .stream()
                    .findFirst()
                    .get();
            if(!userProfile.getName().toLowerCase().contains(requestDetailsHist.getMessage().toLowerCase().split(",")[0])){
                throw new ScenarioNotPossibleException("Old name didn't match");
            }
            userProfile.setName(requestDetailsHist.getName());
            profileRepository.save(userProfile);
            methodToUpdateUserAuthHistTable(user.getId(), reasonCodeIdAssociation.get(ReasonEnum.NAME_CHANGE), requestDetailsHist.getMessage().split(",")[1], adminUserId);
            requestUserHist.setRequestReason(RequestReason.NAME_CHANGE_REQUEST.name());
            methodToUpdateContactUsTable(contactUs, requestUserHist);
        }
    }

    private void methodToUpdateContactUsTable(ContactUs contactUs, ContactUsHist requestUserHist){
        contactUs.setRequestActive(false);
        contactUs.setVerified(true);
        contactUs.setReferenceNumber("COM_" + contactUs.getReferenceNumber());
        contactUs.setRequestStatus(RaiseRequestStatus.COMPLETED.name());
        contactUs.setCompletedTime(LocalDateTime.now());
        ContactUs savedRequest = contactUsRepository.save(contactUs);

        requestUserHist.setContactUsId(savedRequest.getId());
        requestUserHist.setMessage("Admin has been approved");
        requestUserHist.setRequestStatus(RaiseRequestStatus.COMPLETED.name());
        requestUserHist.setUpdatedTime(LocalDateTime.now());
        contactUsHistRepository.save(requestUserHist);
    }

    private void methodToUpdateUserAuthHistTable(Long userId, int reasonTypeId, String comment, Long updatedUserId){
        UserAuthHist userAuthHist = new UserAuthHist();
        userAuthHist.setUserId(userId);
        userAuthHist.setUpdatedTime(LocalDateTime.now());
        userAuthHist.setReasonTypeId(reasonTypeId);
        userAuthHist.setComment(comment);
        userAuthHist.setUpdatedBy(updatedUserId);
        userAuthHistRepository.save(userAuthHist);
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
            requestReason = "All";
        }
        List<UserRequestsGridDto> userRequestsGridDtoList = adminRepository.getUserRequestsGridForAdmin(requestReason);
        userRequestsGridDtoList.forEach(userGrid -> {
            if((status.equalsIgnoreCase("Rename") || status.equalsIgnoreCase("All"))
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
    @Transactional
    public void updateDefectStatus(Long defectId, String status) {
        ContactUs userDefect = contactUsRepository.findById(defectId).orElse(null);
        ContactUsHist userDefectHist = new ContactUsHist();
        if(userDefect == null){
            throw new ResourceNotFoundException("Not able to obtain the user defect details");
        }
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

            userDefectHist.setMessage("Admin ignored. Issue was already solved");
            userDefectHist.setContactUsId(userDefect.getId());
            userDefectHist.setRequestReason(RequestReason.USER_DEFECT_UPDATE.name());
            userDefectHist.setRequestStatus(RaiseRequestStatus.IGNORED.name());
            userDefectHist.setUpdatedTime(userDefect.getCompletedTime());
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
        new Thread(
                () -> userDetails.setImageFromS3(awsServices.fetchUserProfilePictureFromS3(userDetails.getUserId(), username))
        ).start();
        return userDetails;
    }

    @Override
    @Transactional
    public String scheduleNotification(ScheduleNotificationRequestDto requestDto) {
        if(requestDto.getSubject() == null || requestDto.getSubject().isEmpty()){
            throw new ScenarioNotPossibleException("Subject can't be null or empty");
        }
        if(requestDto.getDescription() == null || requestDto.getDescription().isEmpty()){
            throw new ScenarioNotPossibleException("Description can't be null or empty");
        }
        if(requestDto.getScheduleFrom() == null || requestDto.getScheduleTo() == null){
            throw new ScenarioNotPossibleException("From and To dates should not be null");
        }
        if(requestDto.getScheduleTo().isBefore(requestDto.getScheduleFrom())){
            throw new ScenarioNotPossibleException("To Date should be greater than From Date");
        }
        if(requestDto.getRecipients() == null || requestDto.getRecipients().isEmpty()){
            throw new ScenarioNotPossibleException("Recipients should be empty");
        }
        ScheduleNotification scheduleNotification = new ScheduleNotification();
        BeanUtils.copyProperties(requestDto, scheduleNotification);
        scheduleNotification.setActive(true);
        scheduleNotification.setCancelled(false);
        scheduleNotification.setCreatedDate(LocalDateTime.now());
        ScheduleNotification response = scheduleNotificationRepository.save(scheduleNotification);

        if (!requestDto.getRecipients().equalsIgnoreCase("All")) {
            Arrays.stream(requestDto.getRecipients().split(","))
                    .map(String::trim)
                    .map(username -> {
                        UserNotification userNotification = new UserNotification();
                        userNotification.setScheduleId(response.getId());
                        userNotification.setUsername(username);
                        userNotification.setRead(false);
                        return userNotification;
                    })
                    .forEach(userNotificationRepository::save);
        } else {
            new Thread(() -> userRepository.findAll()
                    .stream()
                    .filter(user -> !userRoleAssociation.get(user.getRoleId()).equalsIgnoreCase(UserRoles.ADMIN.name()))
                    .forEach(user -> {
                        UserNotification userNotification = new UserNotification();
                        userNotification.setScheduleId(response.getId());
                        userNotification.setUsername(user.getUsername());
                        userNotification.setRead(false);
                        userNotificationRepository.save(userNotification);
                    })).start();
        }
        return "Notification set successfully";
    }

    @Override
    public List<UserFeedbackResponseDto> getUserFeedbackListForAdmin() {
        AtomicInteger i = new AtomicInteger(1);
        return adminRepository.getUserFeedbackListForAdmin()
                .stream()
                .map(feedback -> {
                    feedback.setRating(Integer.parseInt(feedback.getDescription().substring(0,1)));
                    feedback.setMessage(feedback.getDescription().substring(2));
                    feedback.setId(i.getAndIncrement());
                    return feedback;
                }).toList();
    }

    @Override
    @Transactional
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
    public List<String> getUsernamesOfAllUsers() {
        return userRepository.findAll()
                .stream()
                .filter(user -> userRoleAssociation.get(user.getRoleId()).equalsIgnoreCase(UserRoles.USER.name()))
                .map(UserAuthModel::getUsername)
                .toList();
    }

    @Override
    public List<AdminSchedulesResponseDto> getAllActiveSchedulesOfAdmin() {
        return adminRepository.getAllActiveSchedulesOfAdmin();
    }

    @Override
    @Transactional
    public void cancelTheUserScheduling(Long scheduleId) {
        ScheduleNotification notification = scheduleNotificationRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + scheduleId));
        if (Boolean.TRUE.equals(notification.isCancelled())) {
            throw new IllegalStateException("Schedule with id " + scheduleId + " is already cancelled.");
        }
        notification.setCancelled(true);
        scheduleNotificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void updateAdminPlacedSchedules(AdminScheduleRequestDto requestDto) {
        ScheduleNotification notification = scheduleNotificationRepository.findById(requestDto.getScheduleId())
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + requestDto.getScheduleId()));
        if(requestDto.getSubject() == null || requestDto.getSubject().isEmpty()){
            throw new ScenarioNotPossibleException("Subject can't be null or empty");
        }
        if(requestDto.getDescription() == null || requestDto.getDescription().isEmpty()){
            throw new ScenarioNotPossibleException("Description can't be null or empty");
        }
        if(requestDto.getScheduleFrom() == null || requestDto.getScheduleTo() == null){
            throw new ScenarioNotPossibleException("From and To dates should not be null");
        }
        if(requestDto.getScheduleTo().toLocalDateTime().isBefore(requestDto.getScheduleFrom().toLocalDateTime())){
            throw new ScenarioNotPossibleException("To Date should be greater than From Date");
        }
        if(requestDto.getRecipients() == null || requestDto.getRecipients().isEmpty()){
            throw new ScenarioNotPossibleException("Recipients should be empty");
        }
        notification.setSubject(requestDto.getSubject());
        notification.setDescription(requestDto.getDescription());
        notification.setScheduleFrom(requestDto.getScheduleFrom().toLocalDateTime());
        notification.setScheduleTo(requestDto.getScheduleTo().toLocalDateTime());
        notification.setRecipients(requestDto.getRecipients());
        notification.setDescription("New Update: " + notification.getDescription());
        scheduleNotificationRepository.save(notification);

        userNotificationRepository.findByScheduleId(requestDto.getScheduleId()).forEach(userNotification -> {
            userNotification.setRead(false);
            userNotificationRepository.save(userNotification);
        });
    }

    @Override
    @Transactional
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
        reasonDetails.setCreatedTime(LocalDateTime.now());
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
    @Transactional
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
    @Transactional
    public void deleteReasonByReasonId(int reasonId) {
        ReasonDetails reasonDetails = reasonDetailsRepository.findById(reasonId)
                .orElseThrow(() -> new ResourceNotFoundException("Reason with id " + reasonId + " is not found"));
        reasonDetails.setIsDeleted(true);
        reasonDetailsRepository.save(reasonDetails);
    }
}
