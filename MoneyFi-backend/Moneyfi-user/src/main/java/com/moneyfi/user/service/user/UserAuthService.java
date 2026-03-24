package com.moneyfi.user.service.user;

import com.moneyfi.user.model.auth.UserAuthModel;
import com.moneyfi.user.service.user.dto.request.ChangePasswordDto;
import com.moneyfi.user.service.user.dto.request.UserLoginDetailsRequestDto;
import com.moneyfi.user.service.user.dto.request.UserProfile;
import com.moneyfi.user.service.user.dto.response.RemainingTimeCountDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface UserAuthService {

    UserAuthModel registerUser(UserProfile userProfile, String loginMode, String address);

    ResponseEntity<Map<String, String>> login(@Valid UserLoginDetailsRequestDto requestDto);

    ResponseEntity<Map<String, String>> loginViaGoogleOAuth(Map<String, String> googleAuthToken);

    String loginViaGithubOAuth(String code);

    Long getUserIdByUsername(String username);

    String forgotPassword(String email);

    String verifyCode(String email, String code);

    String updatePasswordOnUserForgotMode(String email, String password);

    RemainingTimeCountDto checkOtpActiveMethod(String email);

    void resendOtp(String username, String otpType);

    Map<String, String> logout(String token);

    String updateUserSessionExpirationTime(long minutes, String username, String substring);

    void changePassword(ChangePasswordDto changePasswordDto);

    ResponseEntity<String> sendOtpToBlockAccount(String username, String type);

    String sendOtpForSignup(String email, String name);

    Boolean checkEnteredOtpDuringSignup(String email, String inputOtp);
}
