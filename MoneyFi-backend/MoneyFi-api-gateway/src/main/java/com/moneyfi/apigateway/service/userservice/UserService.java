package com.moneyfi.apigateway.service.userservice;

import com.moneyfi.apigateway.dto.ChangePasswordDto;
import com.moneyfi.apigateway.dto.ProfileChangePassword;
import com.moneyfi.apigateway.dto.RemainingTimeCountDto;
import com.moneyfi.apigateway.dto.UserProfile;
import com.moneyfi.apigateway.model.UserAuthModel;

public interface UserService {

    UserAuthModel registerUser(UserProfile userProfile);

    Long getUserIdByUsername(String email);

    Long getUserIdFromToken(String token);

    ProfileChangePassword changePassword(ChangePasswordDto changePasswordDto);

    RemainingTimeCountDto checkOtpActiveMethod(String email);

    String sendOtpForSignup(String email, String name);

    boolean checkEnteredOtp(String email, String inputOtp);
}
