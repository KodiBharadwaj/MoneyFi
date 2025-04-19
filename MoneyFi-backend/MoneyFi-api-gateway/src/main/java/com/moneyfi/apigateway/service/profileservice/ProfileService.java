package com.moneyfi.apigateway.service.profileservice;

import com.moneyfi.apigateway.dto.UserProfile;
import com.moneyfi.apigateway.model.ProfileModel;

public interface ProfileService {

    ProfileModel saveUserDetails(Long userId, ProfileModel profile);

    ProfileModel getUserDetailsByUserId(Long userId);

    String getNameByUserId(Long userId);
}
