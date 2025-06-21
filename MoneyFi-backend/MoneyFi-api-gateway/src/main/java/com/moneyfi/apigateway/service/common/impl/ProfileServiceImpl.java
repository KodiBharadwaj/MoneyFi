package com.moneyfi.apigateway.service.common.impl;

import com.moneyfi.apigateway.model.common.ContactUs;
import com.moneyfi.apigateway.model.common.Feedback;
import com.moneyfi.apigateway.model.common.ProfileModel;
import com.moneyfi.apigateway.repository.common.ContactUsRepository;
import com.moneyfi.apigateway.repository.common.FeedbackRepository;
import com.moneyfi.apigateway.repository.common.ProfileRepository;
import com.moneyfi.apigateway.service.common.ProfileService;
import com.moneyfi.apigateway.util.EmailTemplates;
import org.springframework.stereotype.Service;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;
    private final ContactUsRepository contactUsRepository;
    private final FeedbackRepository feedbackRepository;

    public ProfileServiceImpl(ProfileRepository profileRepository,
                              ContactUsRepository contactUsRepository,
                              FeedbackRepository feedbackRepository){
        this.profileRepository = profileRepository;
        this.contactUsRepository = contactUsRepository;
        this.feedbackRepository = feedbackRepository;
    }

    @Override
    public ProfileModel saveUserDetails(Long userId, ProfileModel profile) {
        ProfileModel fetchProfile = profileRepository.findByUserId(userId);

        fetchProfile.setName(profile.getName());
        fetchProfile.setPhone(profile.getPhone());
        fetchProfile.setGender(profile.getGender());
        fetchProfile.setDateOfBirth(profile.getDateOfBirth());
        fetchProfile.setMaritalStatus(profile.getMaritalStatus());
        fetchProfile.setAddress(profile.getAddress());
        fetchProfile.setIncomeRange(profile.getIncomeRange());
        fetchProfile.setProfileImage(profile.getProfileImage());

        return profileRepository.save(fetchProfile);
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

}
