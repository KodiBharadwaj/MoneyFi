package com.moneyfi.apigateway.service.userservice;

import com.moneyfi.apigateway.model.auth.UserAuthModel;
import com.moneyfi.apigateway.service.userservice.dto.*;
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

    boolean getUsernameByDetails(ForgotUsernameDto userDetails);

    void sendAccountStatementEmail(String username, byte[] pdfBytes);
}
