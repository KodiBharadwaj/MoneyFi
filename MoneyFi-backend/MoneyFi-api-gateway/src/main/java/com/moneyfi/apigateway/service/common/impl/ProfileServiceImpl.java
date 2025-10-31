package com.moneyfi.apigateway.service.common.impl;

import com.moneyfi.apigateway.exceptions.ResourceNotFoundException;
import com.moneyfi.apigateway.exceptions.ScenarioNotPossibleException;
import com.moneyfi.apigateway.model.common.ContactUs;
import com.moneyfi.apigateway.model.common.ContactUsHist;
import com.moneyfi.apigateway.model.common.ExcelTemplate;
import com.moneyfi.apigateway.model.common.ProfileModel;
import com.moneyfi.apigateway.repository.common.CommonServiceRepository;
import com.moneyfi.apigateway.repository.user.ContactUsHistRepository;
import com.moneyfi.apigateway.repository.user.ContactUsRepository;
import com.moneyfi.apigateway.repository.user.ExcelTemplateRepository;
import com.moneyfi.apigateway.repository.user.ProfileRepository;
import com.moneyfi.apigateway.service.common.ProfileService;
import com.moneyfi.apigateway.service.common.AwsServices;
import com.moneyfi.apigateway.service.common.dto.request.UserDefectRequestDto;
import com.moneyfi.apigateway.service.common.dto.request.UserFeedbackRequestDto;
import com.moneyfi.apigateway.service.common.dto.response.ProfileDetailsDto;
import com.moneyfi.apigateway.util.EmailTemplates;
import com.moneyfi.apigateway.util.constants.StringUtils;
import com.moneyfi.apigateway.util.enums.RaiseRequestStatus;
import com.moneyfi.apigateway.util.enums.RequestReason;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static com.moneyfi.apigateway.util.constants.StringUtils.generateVerificationCode;
import static com.moneyfi.apigateway.util.constants.StringUtils.templateIdAssociation;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;
    private final ContactUsRepository contactUsRepository;
    private final CommonServiceRepository commonServiceRepository;
    private final ContactUsHistRepository contactUsHistRepository;
    private final ExcelTemplateRepository excelTemplateRepository;
    private final EmailTemplates emailTemplates;
    private final AwsServices awsServices;

    public ProfileServiceImpl(ProfileRepository profileRepository,
                              ContactUsRepository contactUsRepository,
                              CommonServiceRepository commonServiceRepository,
                              ContactUsHistRepository contactUsHistRepository,
                              ExcelTemplateRepository excelTemplateRepository,
                              EmailTemplates emailTemplates,
                              AwsServices awsServices){
        this.profileRepository = profileRepository;
        this.contactUsRepository = contactUsRepository;
        this.commonServiceRepository = commonServiceRepository;
        this.contactUsHistRepository = contactUsHistRepository;
        this.excelTemplateRepository = excelTemplateRepository;
        this.emailTemplates = emailTemplates;
        this.awsServices = awsServices;
    }

    @Override
    @Transactional
    public ProfileDetailsDto saveUserDetails(Long userId, ProfileModel profile) {
        ProfileModel fetchProfile = profileRepository.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("User profile details not found"));
        String phone = profile.getPhone().trim();
        if (!phone.matches("\\d+")) {
            throw new ScenarioNotPossibleException("Phone number must contain only digits");
        }
        if (phone.length() != 10) {
            throw new ScenarioNotPossibleException("Phone number should be 10 digits");
        }

        if (!profile.getName().trim().equals(fetchProfile.getName())) {
            fetchProfile.setName(profile.getName().trim());
        }
        if (!phone.equals(fetchProfile.getPhone())) {
            fetchProfile.setPhone(phone);
        }
        if (!profile.getGender().trim().equals(fetchProfile.getGender())) {
            fetchProfile.setGender(profile.getGender().trim());
        }
        if (!profile.getMaritalStatus().trim().equals(fetchProfile.getMaritalStatus())) {
            fetchProfile.setMaritalStatus(profile.getMaritalStatus().trim());
        }
        if (!profile.getDateOfBirth().equals(fetchProfile.getDateOfBirth())) {
            fetchProfile.setDateOfBirth(profile.getDateOfBirth());
        }
        fetchProfile.setAddress(profile.getAddress());
        fetchProfile.setIncomeRange(profile.getIncomeRange());
        return convertProfileModelToProfileDetailsDto(profileRepository.save(fetchProfile));
    }

    private ProfileDetailsDto convertProfileModelToProfileDetailsDto(ProfileModel savedProfile) {
        ProfileDetailsDto profileDetailsDto = new ProfileDetailsDto();
        BeanUtils.copyProperties(savedProfile, profileDetailsDto);
        profileDetailsDto.setCreatedDate(Date.valueOf(savedProfile.getCreatedDate().toLocalDate()));
        profileDetailsDto.setDateOfBirth(Date.valueOf(savedProfile.getDateOfBirth()));
        return profileDetailsDto;
    }

    @Override
    public ProfileModel getUserDetailsByUserId(Long userId) {
        return profileRepository.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("User profile not found for userId: " + userId));
    }

    @Override
    @Transactional
    public ContactUs saveContactUsDetails(UserDefectRequestDto userDefectRequestDto) {
        ContactUs userDefect = new ContactUs();
        userDefect.setEmail(userDefectRequestDto.getEmail());
        userDefect.setRequestReason(RequestReason.USER_DEFECT_UPDATE.name());
        userDefect.setRequestStatus(RaiseRequestStatus.SUBMITTED.name());
        userDefect.setStartTime(LocalDateTime.now());
        userDefect.setRequestActive(true);
        userDefect.setVerified(false);

        String referenceNumber = StringUtils.generateAlphabetCode() + generateVerificationCode();
        userDefect.setReferenceNumber(referenceNumber);
        userDefect.setImageId("Defect_" + contactUsRepository.save(userDefect).getId() + "_" +
                userDefect.getEmail().substring(0,userDefect.getEmail().indexOf('@')));
        new Thread(() -> {
            emailTemplates.sendUserRaiseDefectEmailToAdmin(userDefectRequestDto, userDefect.getImageId());
            emailTemplates.sendReferenceNumberEmail(userDefectRequestDto.getName(), userDefect.getEmail(), "resolve issue", referenceNumber);
            awsServices.uploadDefectPictureByUser(userDefect.getImageId(), userDefectRequestDto.getFile());
        }).start();
        ContactUs savedDefect = contactUsRepository.save(userDefect);

        ContactUsHist userDefectHist = new ContactUsHist();
        userDefectHist.setContactUsId(savedDefect.getId());
        userDefectHist.setName(userDefectRequestDto.getName());
        userDefectHist.setMessage(userDefectRequestDto.getMessage());
        userDefectHist.setRequestReason(RequestReason.USER_DEFECT_UPDATE.name());
        userDefectHist.setRequestStatus(RaiseRequestStatus.SUBMITTED.name());
        userDefectHist.setUpdatedTime(savedDefect.getStartTime());
        contactUsHistRepository.save(userDefectHist);
        return savedDefect;
    }

    @Override
    public ContactUs saveFeedback(UserFeedbackRequestDto feedback) {
        String rating = feedback.getMessage().substring(0,1);
        String message = feedback.getMessage().substring(2);
        new Thread(() ->
                emailTemplates.sendUserFeedbackEmailToAdmin(rating , message)
        ).start();

        ContactUs userFeedback = new ContactUs();
        userFeedback.setEmail(feedback.getEmail());
        userFeedback.setRequestActive(true);
        userFeedback.setVerified(false);
        userFeedback.setRequestReason(RequestReason.USER_FEEDBACK_UPDATE.name());
        userFeedback.setRequestStatus(RaiseRequestStatus.SUBMITTED.name());
        userFeedback.setStartTime(LocalDateTime.now());
        ContactUs savedFeedback = contactUsRepository.save(userFeedback);

        ContactUsHist userFeedbackHist = new ContactUsHist();
        userFeedbackHist.setContactUsId(savedFeedback.getId());
        userFeedbackHist.setMessage(feedback.getMessage());
        userFeedbackHist.setName(feedback.getName());
        userFeedbackHist.setRequestReason(RequestReason.USER_FEEDBACK_UPDATE.name());
        userFeedbackHist.setRequestStatus(RaiseRequestStatus.SUBMITTED.name());
        userFeedbackHist.setUpdatedTime(savedFeedback.getStartTime());
        contactUsHistRepository.save(userFeedbackHist);

        return savedFeedback;
    }

    @Override
    public ProfileDetailsDto getProfileDetailsOfUser(String username) {
        ProfileDetailsDto profileDetailsDto = commonServiceRepository.getProfileDetailsOfUser(username);
        if(profileDetailsDto == null){
            throw new ResourceNotFoundException("Details not found for " + username);
        }
        return profileDetailsDto;
    }

    @Override
    public ResponseEntity<String> parseUserProfileDataFromExcel(MultipartFile excel, Long userId) {
        try (InputStream is = excel.getInputStream()) {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheetAt(0);
            Row row = sheet.getRow(1);
            java.util.Date dobDate = row.getCell(1).getDateCellValue();
            double incomeRange = row.getCell(4).getNumericCellValue();
            BigDecimal phoneCellValue = BigDecimal.valueOf(row.getCell(0).getNumericCellValue());
            String phoneNumber = phoneCellValue.toBigInteger().toString();
            if (phoneNumber.length() != 10) {
                throw new ScenarioNotPossibleException("Phone number should be 10 digits");
            }
            ProfileModel fetchProfile = profileRepository.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("User profile not found for userId: " + userId));
            if (fetchProfile != null) {
                fetchProfile.setPhone(phoneNumber);
                fetchProfile.setDateOfBirth(dobDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                fetchProfile.setGender(row.getCell(2).getStringCellValue());
                fetchProfile.setMaritalStatus(row.getCell(3).getStringCellValue());
                fetchProfile.setIncomeRange(incomeRange);
                fetchProfile.setAddress(row.getCell(5).getStringCellValue());
                profileRepository.save(fetchProfile);
            } else {
                throw new ResourceNotFoundException("User profile not found for userId: " + userId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResourceNotFoundException("Invalid Excel format");
        }
        return null;
    }

    @Override
    public ResponseEntity<byte[]> downloadTemplateForUserProfile() {
        ExcelTemplate template = excelTemplateRepository.findById(templateIdAssociation.get("profile-template"))
                .orElseThrow(() -> new RuntimeException("Template not found"));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + template.getName())
                .contentType(MediaType.parseMediaType(template.getContentType()))
                .body(template.getContent());
    }
}
