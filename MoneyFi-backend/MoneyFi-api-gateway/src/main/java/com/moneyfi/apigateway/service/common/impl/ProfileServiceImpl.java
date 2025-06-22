package com.moneyfi.apigateway.service.common.impl;

import com.moneyfi.apigateway.model.common.ContactUs;
import com.moneyfi.apigateway.model.common.Feedback;
import com.moneyfi.apigateway.model.common.ProfileModel;
import com.moneyfi.apigateway.repository.common.CommonServiceRepository;
import com.moneyfi.apigateway.repository.common.ContactUsRepository;
import com.moneyfi.apigateway.repository.common.FeedbackRepository;
import com.moneyfi.apigateway.repository.common.ProfileRepository;
import com.moneyfi.apigateway.service.common.ProfileService;
import com.moneyfi.apigateway.service.common.dto.ProfileDetailsDto;
import com.moneyfi.apigateway.util.EmailTemplates;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.sql.Date;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;
    private final ContactUsRepository contactUsRepository;
    private final FeedbackRepository feedbackRepository;
    private final CommonServiceRepository commonServiceRepository;

    public ProfileServiceImpl(ProfileRepository profileRepository,
                              ContactUsRepository contactUsRepository,
                              FeedbackRepository feedbackRepository,
                              CommonServiceRepository commonServiceRepository){
        this.profileRepository = profileRepository;
        this.contactUsRepository = contactUsRepository;
        this.feedbackRepository = feedbackRepository;
        this.commonServiceRepository = commonServiceRepository;
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
        fetchProfile.setProfileImage(profile.getProfileImage());

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
    public ContactUs saveContactUsDetails(ContactUs contactUsDetails) {
        new Thread(() ->
                EmailTemplates.sendContactAlertMail(contactUsDetails, contactUsDetails.getImages())
        ).start();

        return contactUsRepository.save(contactUsDetails);
    }

    @Override
    public Feedback saveFeedback(Feedback feedback) {
        new Thread(() ->
                EmailTemplates.feedbackAlertMail(feedback)
        ).start();

        return feedbackRepository.save(feedback);
    }

    @Override
    public ProfileDetailsDto getProfileDetailsOfUser(Long userId) {
        return commonServiceRepository.getProfileDetailsOfUser(userId);
    }

}
