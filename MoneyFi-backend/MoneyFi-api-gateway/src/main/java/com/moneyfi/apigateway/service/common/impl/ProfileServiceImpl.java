package com.moneyfi.apigateway.service.common.impl;

import com.moneyfi.apigateway.model.common.ContactUs;
import com.moneyfi.apigateway.model.common.ProfileModel;
import com.moneyfi.apigateway.repository.common.CommonServiceRepository;
import com.moneyfi.apigateway.repository.user.ContactUsRepository;
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
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Date;

import static com.moneyfi.apigateway.util.constants.StringUtils.generateVerificationCode;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;
    private final ContactUsRepository contactUsRepository;
    private final CommonServiceRepository commonServiceRepository;
    private final S3AwsService s3AwsService;

    public ProfileServiceImpl(ProfileRepository profileRepository,
                              ContactUsRepository contactUsRepository,
                              CommonServiceRepository commonServiceRepository,
                              S3AwsService s3AwsService){
        this.profileRepository = profileRepository;
        this.contactUsRepository = contactUsRepository;
        this.commonServiceRepository = commonServiceRepository;
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
        ContactUs contactUsDetails = new ContactUs();
        contactUsDetails.setName(userDefectRequestDto.getName());
        contactUsDetails.setEmail(userDefectRequestDto.getEmail());
        contactUsDetails.setMessage(userDefectRequestDto.getMessage());
        contactUsDetails.setRequestReason(RequestReason.USER_DEFECT_UPDATE.name());
        contactUsDetails.setRequestStatus(RaiseRequestStatus.SUBMITTED.name());
        contactUsDetails.setRequestActive(true);
        contactUsDetails.setVerified(false);

        String referenceNumber = StringUtils.generateAlphabetCode() + generateVerificationCode();
        contactUsDetails.setReferenceNumber(referenceNumber);
        contactUsDetails.setImageId("Defect_user_" +
                contactUsDetails.getEmail().substring(0,contactUsDetails.getEmail().indexOf('@')));
        new Thread(() -> {
            EmailTemplates.sendContactAlertMail(contactUsDetails, contactUsDetails.getImageId());
            EmailTemplates.sendReferenceNumberEmail(contactUsDetails.getName(), contactUsDetails.getEmail(), "resolve issue", referenceNumber);
            s3AwsService.uploadDefectPictureByUser(contactUsDetails.getImageId(), userDefectRequestDto.getFile());
        }).start();
        return contactUsRepository.save(contactUsDetails);
    }

    @Override
    public ContactUs saveFeedback(ContactUs feedback) {
        String rating = feedback.getMessage().substring(0,1);
        String message = feedback.getMessage().substring(2);
        new Thread(() ->
                EmailTemplates.feedbackAlertMail(rating , message)
        ).start();

        feedback.setRequestReason(RequestReason.USER_FEEDBACK_UPDATE.name());
        feedback.setRequestStatus(RaiseRequestStatus.SUBMITTED.name());
        feedback.setRequestActive(true);
        feedback.setVerified(false);
        return contactUsRepository.save(feedback);
    }

    @Override
    public ProfileDetailsDto getProfileDetailsOfUser(Long userId) {
        return commonServiceRepository.getProfileDetailsOfUser(userId);
    }

}
