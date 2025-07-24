package com.moneyfi.apigateway.service.common;

import com.moneyfi.apigateway.model.common.ContactUs;
import com.moneyfi.apigateway.model.common.ProfileModel;
import com.moneyfi.apigateway.service.common.dto.response.ProfileDetailsDto;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileService {

    ProfileDetailsDto saveUserDetails(Long userId, ProfileModel profile);

    ProfileModel getUserDetailsByUserId(Long userId);

    ContactUs saveContactUsDetails(ContactUs contactUsDetails, MultipartFile file);

    ContactUs saveFeedback(ContactUs feedback);

    ProfileDetailsDto getProfileDetailsOfUser(Long userId);
}
