package com.moneyfi.apigateway.service.common;

import com.moneyfi.apigateway.model.common.ContactUs;
import com.moneyfi.apigateway.model.common.Feedback;
import com.moneyfi.apigateway.model.common.ProfileModel;
import com.moneyfi.apigateway.service.common.dto.response.ProfileDetailsDto;

public interface ProfileService {

    ProfileDetailsDto saveUserDetails(Long userId, ProfileModel profile);

    ProfileModel getUserDetailsByUserId(Long userId);

    ContactUs saveContactUsDetails(ContactUs contactUsDetails);

    Feedback saveFeedback(Feedback feedback);

    ProfileDetailsDto getProfileDetailsOfUser(Long userId);
}
