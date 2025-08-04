package com.moneyfi.apigateway.service.common.impl;

import com.moneyfi.apigateway.exceptions.ResourceNotFoundException;
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
import com.moneyfi.apigateway.service.common.S3AwsService;
import com.moneyfi.apigateway.service.common.dto.request.UserDefectRequestDto;
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
    private final S3AwsService s3AwsService;

    public ProfileServiceImpl(ProfileRepository profileRepository,
                              ContactUsRepository contactUsRepository,
                              CommonServiceRepository commonServiceRepository,
                              ContactUsHistRepository contactUsHistRepository,
                              ExcelTemplateRepository excelTemplateRepository,
                              S3AwsService s3AwsService){
        this.profileRepository = profileRepository;
        this.contactUsRepository = contactUsRepository;
        this.commonServiceRepository = commonServiceRepository;
        this.contactUsHistRepository = contactUsHistRepository;
        this.excelTemplateRepository = excelTemplateRepository;
        this.s3AwsService = s3AwsService;
    }

    @Override
    public ProfileDetailsDto saveUserDetails(Long userId, ProfileModel profile) {
        ProfileModel fetchProfile = profileRepository.findByUserId(userId);

        fetchProfile.setName(profile.getName());
        fetchProfile.setPhone(profile.getPhone());
        fetchProfile.setGender(profile.getGender());
        fetchProfile.setDateOfBirth(profile.getDateOfBirth());
        fetchProfile.setMaritalStatus(profile.getMaritalStatus());
        fetchProfile.setAddress(profile.getAddress());
        fetchProfile.setIncomeRange(profile.getIncomeRange());

        ProfileModel savedProfile = profileRepository.save(fetchProfile);
        return convertProfileModelToProfileDetailsDto(savedProfile);
    }

    private ProfileDetailsDto convertProfileModelToProfileDetailsDto(ProfileModel savedProfile){
        ProfileDetailsDto profileDetailsDto = new ProfileDetailsDto();
        BeanUtils.copyProperties(savedProfile, profileDetailsDto);
        profileDetailsDto.setCreatedDate(Date.valueOf(savedProfile.getCreatedDate().toLocalDate()));
        profileDetailsDto.setDateOfBirth(Date.valueOf(savedProfile.getDateOfBirth()));
        return profileDetailsDto;
    }

    @Override
    public ProfileModel getUserDetailsByUserId(Long userId) {
        return profileRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public ContactUs saveContactUsDetails(UserDefectRequestDto userDefectRequestDto) {
        ContactUs userDefect = new ContactUs();
        userDefect.setEmail(userDefectRequestDto.getEmail());
        userDefect.setRequestReason(RequestReason.USER_DEFECT_UPDATE.name());
        userDefect.setRequestStatus(RaiseRequestStatus.SUBMITTED.name());
        userDefect.setRequestActive(true);
        userDefect.setVerified(false);

        String referenceNumber = StringUtils.generateAlphabetCode() + generateVerificationCode();
        userDefect.setReferenceNumber(referenceNumber);
        userDefect.setImageId("Defect_" + contactUsRepository.save(userDefect).getId() + "_" +
                userDefect.getEmail().substring(0,userDefect.getEmail().indexOf('@')));
        new Thread(() -> {
            EmailTemplates.sendContactAlertMail(userDefectRequestDto, userDefect.getImageId());
            EmailTemplates.sendReferenceNumberEmail(userDefectRequestDto.getName(), userDefect.getEmail(), "resolve issue", referenceNumber);
            s3AwsService.uploadDefectPictureByUser(userDefect.getImageId(), userDefectRequestDto.getFile());
        }).start();
        ContactUs savedDefect = contactUsRepository.save(userDefect);

        ContactUsHist userDefectHist = new ContactUsHist();
        userDefectHist.setContactUsId(savedDefect.getId());
        userDefectHist.setName(userDefectRequestDto.getName());
        userDefectHist.setMessage(userDefectRequestDto.getMessage());
        contactUsHistRepository.save(userDefectHist);
        return savedDefect;
    }

//    @Override
//    public ContactUs saveFeedback(ContactUs feedback) {
//        String rating = feedback.getMessage().substring(0,1);
//        String message = feedback.getMessage().substring(2);
//        new Thread(() ->
//                EmailTemplates.feedbackAlertMail(rating , message)
//        ).start();
//
//        feedback.setRequestReason(RequestReason.USER_FEEDBACK_UPDATE.name());
//        feedback.setRequestStatus(RaiseRequestStatus.SUBMITTED.name());
//        feedback.setRequestActive(true);
//        feedback.setVerified(false);
//        return contactUsRepository.save(feedback);
//    }

    @Override
    public ProfileDetailsDto getProfileDetailsOfUser(Long userId) {
        return commonServiceRepository.getProfileDetailsOfUser(userId);
    }

    @Override
    public ResponseEntity<String> parseUserProfileDataFromExcel(MultipartFile excel, Long userId) {
        try (InputStream is = excel.getInputStream()) {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheetAt(0);
            Row row = sheet.getRow(1);

            java.util.Date dobDate = row.getCell(1).getDateCellValue();
            double incomeRange = row.getCell(4).getNumericCellValue();

            ProfileModel fetchProfile = profileRepository.findByUserId(userId);

            if (fetchProfile != null) {
                fetchProfile.setPhone(String.valueOf(BigDecimal.valueOf(row.getCell(0).getNumericCellValue()).toBigInteger()));
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
