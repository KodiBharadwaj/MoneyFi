package com.moneyfi.apigateway.service.userservice;

import com.moneyfi.apigateway.model.auth.UserAuthModel;
import com.moneyfi.apigateway.service.userservice.dto.request.*;
import com.moneyfi.apigateway.service.userservice.dto.response.RemainingTimeCountDto;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface UserService {

    UserAuthModel registerUser(UserProfile userProfile, String loginMode, String address);

    ResponseEntity<Map<String, String>> login(UserLoginDetailsRequestDto requestDto);

    ResponseEntity<Map<String, String>> loginViaGoogleOAuth(Map<String, String> googleAuthToken);

    String loginViaGithubOAuth(String code);

    Long getUserIdByUsername(String email);

    void changePassword(ChangePasswordDto changePasswordDto);

    RemainingTimeCountDto checkOtpActiveMethod(String email);

    String sendOtpForSignup(String email, String name);

    Boolean checkEnteredOtpDuringSignup(String email, String inputOtp);

    Map<String, String> logout(String token);

    ResponseEntity<String> sendOtpToBlockAccount(String username, String type);

    String updateUserSessionExpirationTime(long minutes, String username);
}
