package com.moneyfi.apigateway.service.userservice;

import com.moneyfi.apigateway.model.auth.UserAuthModel;
import com.moneyfi.apigateway.service.userservice.dto.request.AccountBlockRequestDto;
import com.moneyfi.apigateway.service.userservice.dto.request.ChangePasswordDto;
import com.moneyfi.apigateway.service.userservice.dto.request.ForgotUsernameDto;
import com.moneyfi.apigateway.service.userservice.dto.request.UserProfile;
import com.moneyfi.apigateway.service.userservice.dto.response.ProfileChangePassword;
import com.moneyfi.apigateway.service.userservice.dto.response.RemainingTimeCountDto;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface UserService {

    UserAuthModel registerUser(UserProfile userProfile, String loginMode);

    ResponseEntity<Map<String, String>> login(UserAuthModel userAuthModel);

    ResponseEntity<Map<String, String>> loginViaGoogleOAuth(Map<String, String> googleAuthToken);

    Long getUserIdByUsername(String email);

    ProfileChangePassword changePassword(ChangePasswordDto changePasswordDto);

    RemainingTimeCountDto checkOtpActiveMethod(String email);

    String sendOtpForSignup(String email, String name);

    boolean checkEnteredOtp(String email, String inputOtp);

    Map<String, String> logout(String token);

    boolean getUsernameByDetails(ForgotUsernameDto userDetails);

    boolean sendAccountStatementEmail(String username, byte[] pdfBytes);

    String uploadUserProfilePictureToS3(String username, MultipartFile file) throws IOException;

    ResponseEntity<ByteArrayResource> fetchUserProfilePictureFromS3(String username);

    ResponseEntity<String> deleteProfilePictureFromS3(String username);

    ResponseEntity<String> blockAccountByUserRequest(String username, AccountBlockRequestDto request);

    ResponseEntity<String> sendOtpToBlockAccount(String username);
}
