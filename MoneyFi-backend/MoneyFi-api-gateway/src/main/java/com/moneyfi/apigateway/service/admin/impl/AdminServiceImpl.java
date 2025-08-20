package com.moneyfi.apigateway.service.admin.impl;

import com.moneyfi.apigateway.exceptions.ResourceNotFoundException;
import com.moneyfi.apigateway.exceptions.ScenarioNotPossibleException;
import com.moneyfi.apigateway.model.auth.UserAuthModel;
import com.moneyfi.apigateway.model.common.*;
import com.moneyfi.apigateway.repository.admin.AdminRepository;
import com.moneyfi.apigateway.repository.user.*;
import com.moneyfi.apigateway.repository.user.auth.UserRepository;
import com.moneyfi.apigateway.service.admin.AdminService;
import com.moneyfi.apigateway.service.admin.dto.request.ScheduleNotificationRequestDto;
import com.moneyfi.apigateway.service.admin.dto.response.*;
import com.moneyfi.apigateway.service.common.S3AwsService;
import com.moneyfi.apigateway.util.enums.RaiseRequestStatus;
import com.moneyfi.apigateway.util.enums.RequestReason;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final ContactUsRepository contactUsRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ContactUsHistRepository contactUsHistRepository;
    private final ScheduleNotificationRepository scheduleNotificationRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final S3AwsService s3AwsService;

    public AdminServiceImpl(AdminRepository adminRepository,
                            ContactUsRepository contactUsRepository,
                            UserRepository userRepository,
                            ProfileRepository profileRepository,
                            ContactUsHistRepository contactUsHistRepository,
                            ScheduleNotificationRepository scheduleNotificationRepository,
                            UserNotificationRepository userNotificationRepository,
                            S3AwsService s3AwsService){
        this.adminRepository = adminRepository;
        this.contactUsRepository = contactUsRepository;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.contactUsHistRepository = contactUsHistRepository;
        this.scheduleNotificationRepository = scheduleNotificationRepository;
        this.userNotificationRepository = userNotificationRepository;
        this.s3AwsService = s3AwsService;
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
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy")); // Change format as needed
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
    public boolean accountReactivationAndNameChangeRequest(String email, String referenceNumber, String requestStatus) {
        return contactUsRepository.findByEmail(email)
                .stream()
                .filter(ContactUs::isRequestActive)
                .filter(i -> i.getReferenceNumber() != null &&
                        i.getReferenceNumber().trim().equalsIgnoreCase(referenceNumber.trim()))
                .findFirst()
                .map(i -> {
                    functionCallToChangeDetails(email, i, requestStatus);
                    return true;
                })
                .orElse(false);
    }

    private void functionCallToChangeDetails(String email, ContactUs contactUs, String requestStatus){
        UserAuthModel user = userRepository.getUserDetailsByUsername(email);
        ContactUsHist requestUserHist = new ContactUsHist();

        if(requestStatus.equalsIgnoreCase(RequestReason.ACCOUNT_UNBLOCK_REQUEST.name())){
            user.setBlocked(false);
            userRepository.save(user);

            requestUserHist.setRequestReason(RequestReason.ACCOUNT_UNBLOCK_REQUEST.name());
            methodToUpdateContactUsTable(contactUs, requestUserHist);
        } else if (requestStatus.equalsIgnoreCase(RequestReason.ACCOUNT_NOT_DELETE_REQUEST.name())){
            user.setDeleted(false);
            userRepository.save(user);

            requestUserHist.setRequestReason(RequestReason.ACCOUNT_NOT_DELETE_REQUEST.name());
            methodToUpdateContactUsTable(contactUs, requestUserHist);
        } else if (requestStatus.equalsIgnoreCase(RequestReason.NAME_CHANGE_REQUEST.name())){
            ProfileModel userProfile = profileRepository.findByUserId(user.getId());
            ContactUsHist requestDetailsHist = contactUsHistRepository.findByContactUsIdList(contactUs.getId())
                    .stream()
                    .findFirst()
                    .get();
            if(!userProfile.getName().toLowerCase().contains(requestDetailsHist.getMessage().toLowerCase())){
                throw new ScenarioNotPossibleException("Old name didn't match");
            }

            userProfile.setName(requestDetailsHist.getName());
            profileRepository.save(userProfile);

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
                () -> userDetails.setImageFromS3(s3AwsService.fetchUserProfilePictureFromS3(userDetails.getUserId(), username))
        ).start();
        return userDetails;
    }

    @Override
    @Transactional
    public String scheduleNotification(ScheduleNotificationRequestDto requestDto) {
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
        }
        return "Notification set successfully";
    }
}
