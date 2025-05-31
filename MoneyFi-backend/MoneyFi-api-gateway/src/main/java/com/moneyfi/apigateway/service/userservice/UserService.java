package com.moneyfi.apigateway.service.userservice;

import com.moneyfi.apigateway.dto.*;
import com.moneyfi.apigateway.model.auth.UserAuthModel;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface UserService {

    UserAuthModel registerUser(UserProfile userProfile);

    ResponseEntity<?> login(UserAuthModel userAuthModel);

    Long getUserIdByUsername(String email);

    ProfileChangePassword changePassword(ChangePasswordDto changePasswordDto);

    RemainingTimeCountDto checkOtpActiveMethod(String email);

    String sendOtpForSignup(String email, String name);

    boolean checkEnteredOtp(String email, String inputOtp);

    Map<String, String> logout(String token);

    String getUsernameByDetails(ForgotUsernameDto userDetails);
}
