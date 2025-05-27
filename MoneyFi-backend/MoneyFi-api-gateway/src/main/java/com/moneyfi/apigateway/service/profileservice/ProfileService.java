package com.moneyfi.apigateway.service.profileservice;

import com.moneyfi.apigateway.model.common.ContactUs;
import com.moneyfi.apigateway.model.common.Feedback;
import com.moneyfi.apigateway.model.common.ProfileModel;

public interface ProfileService {

    ProfileModel saveUserDetails(Long userId, ProfileModel profile);

    ProfileModel getUserDetailsByUserId(Long userId);

    String getNameByUserId(Long userId);

    ContactUs saveContactUsDetails(ContactUs contactUsDetails);

    Feedback saveFeedback(Feedback feedback);
}
