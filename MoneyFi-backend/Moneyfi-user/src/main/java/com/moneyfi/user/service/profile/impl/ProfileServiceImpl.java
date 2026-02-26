package com.moneyfi.user.service.profile.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.moneyfi.user.exceptions.ResourceNotFoundException;
import com.moneyfi.user.exceptions.ScenarioNotPossibleException;
import com.moneyfi.user.model.ContactUs;
import com.moneyfi.user.model.ContactUsHist;
import com.moneyfi.user.model.ExcelTemplate;
import com.moneyfi.user.model.ProfileModel;
import com.moneyfi.user.repository.ContactUsHistRepository;
import com.moneyfi.user.repository.ContactUsRepository;
import com.moneyfi.user.repository.ExcelTemplateRepository;
import com.moneyfi.user.repository.ProfileRepository;
import com.moneyfi.user.repository.common.CommonServiceRepository;
import com.moneyfi.user.service.common.AwsServices;
import com.moneyfi.user.service.common.CloudinaryService;
import com.moneyfi.user.service.common.RabbitMqQueuePublisher;
import com.moneyfi.user.service.common.dto.emaildto.UserRaisedDefectDto;
import com.moneyfi.user.service.common.dto.internal.NotificationQueueDto;
import com.moneyfi.user.service.common.dto.request.UserDefectRequestDto;
import com.moneyfi.user.service.common.dto.request.UserFeedbackRequestDto;
import com.moneyfi.user.service.profile.ProfileService;
import com.moneyfi.user.service.profile.dto.ProfileDetailsDto;
import com.moneyfi.user.util.constants.StringConstants;
import com.moneyfi.user.util.enums.NotificationQueueEnum;
import com.moneyfi.user.util.enums.RaiseRequestStatus;
import com.moneyfi.user.util.enums.RequestReason;
import com.moneyfi.user.validator.UserValidations;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;

import static com.moneyfi.user.util.constants.StringConstants.*;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    @Autowired
    private RabbitMqQueuePublisher rabbitMqQueuePublisher;

    private static final String EMAIL_MISMATCH_MESSAGE = "Email mismatch detected";
    private static final String TEMPLATE_NOT_FOUND_MESSAGE = "Template not found";

    private final ProfileRepository profileRepository;
    private final ContactUsRepository contactUsRepository;
    private final CommonServiceRepository commonServiceRepository;
    private final ContactUsHistRepository contactUsHistRepository;
    private final ExcelTemplateRepository excelTemplateRepository;
    private final AwsServices awsServices;
    private final CloudinaryService cloudinaryService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional(rollbackOn = Exception.class)
    public ProfileDetailsDto saveUserDetails(Long userId, ProfileModel profile) {
        ProfileModel fetchProfile = profileRepository.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException(USER_PROFILE_DETAILS_NOT_FOUND));

        if (!profile.getName().trim().equals(fetchProfile.getName())) {
            fetchProfile.setName(profile.getName().trim());
        }
        String phone = profile.getPhone() != null ? profile.getPhone().trim() : null;
        if (phone != null && !phone.equals(fetchProfile.getPhone())) {
            UserValidations.checkPhoneNumberValidations(phone);
            fetchProfile.setPhone(phone);
        }
        if (profile.getGender() != null && !profile.getGender().trim().equals(fetchProfile.getGender())) {
            fetchProfile.setGender(profile.getGender().trim());
        }
        if (profile.getMaritalStatus() != null && !profile.getMaritalStatus().trim().equals(fetchProfile.getMaritalStatus())) {
            fetchProfile.setMaritalStatus(profile.getMaritalStatus().trim());
        }
        if (profile.getDateOfBirth() != null && !profile.getDateOfBirth().equals(fetchProfile.getDateOfBirth())) {
            fetchProfile.setDateOfBirth(profile.getDateOfBirth());
        }
        fetchProfile.setAddress(profile.getAddress());
        fetchProfile.setIncomeRange(profile.getIncomeRange());
        return convertProfileModelToProfileDetailsDto(profileRepository.save(fetchProfile));
    }

    @Override
    public ProfileDetailsDto getProfileDetailsOfUser(String username) {
        ProfileDetailsDto profileDetailsDto = commonServiceRepository.getProfileDetailsOfUser(username);
        if (profileDetailsDto == null) {
            throw new ResourceNotFoundException(USER_PROFILE_NOT_FOUND);
        }
        return profileDetailsDto;
    }

    @Override
    public String getUserDetailsByUserId(Long userId) {
        return profileRepository.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException(USER_PROFILE_DETAILS_NOT_FOUND)).getName();
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void saveContactUsDetails(UserDefectRequestDto userDefectRequestDto, Long userId, String username) throws IOException {
        if (!username.equals(userDefectRequestDto.getEmail().trim())) {
            throw new BadRequestException(EMAIL_MISMATCH_MESSAGE);
        }
        String referenceNumber = StringConstants.generateAlphabetCode() + generateVerificationCode();

        ContactUs userDefect = new ContactUs();
        userDefect.setEmail(userDefectRequestDto.getEmail());
        userDefect.setRequestReason(RequestReason.USER_DEFECT_UPDATE.name());
        userDefect.setRequestStatus(RaiseRequestStatus.SUBMITTED.name());
        userDefect.setStartTime(LocalDateTime.now());
        userDefect.setRequestActive(true);
        userDefect.setVerified(false);
        userDefect.setReferenceNumber(referenceNumber);
        ContactUs savedDefect = contactUsRepository.save(userDefect);

        ContactUsHist userDefectHist = new ContactUsHist();
        userDefectHist.setContactUsId(savedDefect.getId());
        userDefectHist.setName(userDefectRequestDto.getName());
        userDefectHist.setMessage(userDefectRequestDto.getMessage());
        userDefectHist.setRequestReason(savedDefect.getRequestReason());
        userDefectHist.setRequestStatus(savedDefect.getRequestStatus());
        userDefectHist.setUpdatedTime(savedDefect.getStartTime());
        userDefectHist.setUpdatedBy(userId);
        contactUsHistRepository.save(userDefectHist);

        if (userDefectRequestDto.getFile() != null && !userDefectRequestDto.getFile().isEmpty()) {
            if (LOCAL_PROFILE.equalsIgnoreCase(activeProfile)) {
                cloudinaryService.uploadPictureToCloudinary(userDefectRequestDto.getFile(), savedDefect.getId(), userDefectRequestDto.getEmail().trim(), UPLOAD_USER_RAISED_REPORT_PICTURE);
            } else {
                awsServices.uploadPictureToS3(savedDefect.getId(), userDefectRequestDto.getEmail().trim(), userDefectRequestDto.getFile(), UPLOAD_USER_RAISED_REPORT_PICTURE);
            }
        }
        applicationEventPublisher.publishEvent(new NotificationQueueDto(NotificationQueueEnum.SEND_REFERENCE_NUMBER_TO_USER_MAIL.name(), userDefectRequestDto.getName() + "<|>" + userDefect.getEmail() + "<|>" + "resolve issue" + "<|>" + referenceNumber));
        String base64Image = null;
        String fileName = null;
        String contentType = null;
        if (userDefectRequestDto.getFile() != null && !userDefectRequestDto.getFile().isEmpty()) {
            MultipartFile file = userDefectRequestDto.getFile();
            base64Image = Base64.getEncoder().encodeToString(file.getBytes());
            fileName = file.getOriginalFilename();
            contentType = file.getContentType();
        }
        applicationEventPublisher.publishEvent(new NotificationQueueDto(NotificationQueueEnum.USER_RAISED_DEFECT_TO_ADMIN_MAIL.name(), objectMapper.writeValueAsString(new UserRaisedDefectDto(userDefectRequestDto.getMessage(), userDefectRequestDto.getName(), userDefectRequestDto.getEmail(), base64Image, fileName, contentType))));
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void saveFeedback(UserFeedbackRequestDto feedback, Long userId) {
        String rating = feedback.getMessage().substring(0, 1);
        String message = feedback.getMessage().substring(2);

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
        userFeedbackHist.setRequestReason(savedFeedback.getRequestReason());
        userFeedbackHist.setRequestStatus(savedFeedback.getRequestStatus());
        userFeedbackHist.setUpdatedTime(savedFeedback.getStartTime());
        userFeedbackHist.setUpdatedBy(userId);
        contactUsHistRepository.save(userFeedbackHist);
        applicationEventPublisher.publishEvent(new NotificationQueueDto(NotificationQueueEnum.USER_FEEDBACK_MAIL.name(), rating + "<|>" + (StringUtils.isBlank(message) ? "NULL" : message)));
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void parseUserProfileDataFromExcel(MultipartFile excel, Long userId) {
        try (InputStream is = excel.getInputStream()) {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheetAt(0);
            Row row = sheet.getRow(1);
            java.util.Date dobDate = row.getCell(1).getDateCellValue();
            double incomeRange = row.getCell(4).getNumericCellValue();
            BigDecimal phoneCellValue = BigDecimal.valueOf(row.getCell(0).getNumericCellValue());
            String phoneNumber = phoneCellValue.toBigInteger().toString();
            if (phoneNumber.length() != 10) {
                throw new ScenarioNotPossibleException(PHONE_NUMBER_MAX_LENGTH_MESSAGE);
            }
            ProfileModel fetchProfile = profileRepository.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException(USER_PROFILE_NOT_FOUND));
            fetchProfile.setPhone(phoneNumber);
            fetchProfile.setDateOfBirth(dobDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            fetchProfile.setGender(row.getCell(2).getStringCellValue());
            fetchProfile.setMaritalStatus(row.getCell(3).getStringCellValue());
            fetchProfile.setIncomeRange(incomeRange);
            fetchProfile.setAddress(row.getCell(5).getStringCellValue());
            profileRepository.save(fetchProfile);
        } catch (ScenarioNotPossibleException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResourceNotFoundException(INVALID_EXCEL_FORMAT);
        }
    }

    @Override
    public ResponseEntity<byte[]> downloadTemplateForUserProfile() {
        ExcelTemplate template = excelTemplateRepository.findById(templateIdAssociation.get(PROFILE_TEMPLATE_NAME))
                .orElseThrow(() -> new RuntimeException(TEMPLATE_NOT_FOUND_MESSAGE));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + template.getName())
                .contentType(MediaType.parseMediaType(template.getContentType()))
                .body(template.getContent());
    }

    private ProfileDetailsDto convertProfileModelToProfileDetailsDto(ProfileModel savedProfile) {
        ProfileDetailsDto profileDetailsDto = new ProfileDetailsDto();
        BeanUtils.copyProperties(savedProfile, profileDetailsDto);
        profileDetailsDto.setCreatedDate(Date.valueOf(savedProfile.getCreatedDate().toLocalDate()));
        profileDetailsDto.setDateOfBirth(Date.valueOf(savedProfile.getDateOfBirth()));
        return profileDetailsDto;
    }
}
