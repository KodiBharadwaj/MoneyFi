package com.moneyfi.apigateway.service.userservice;

import com.moneyfi.apigateway.model.auth.UserAuthModel;
import com.moneyfi.apigateway.service.userservice.dto.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    boolean sendAccountStatementEmail(String username, byte[] pdfBytes);

    String uploadUserProfilePictureToS3(String username, MultipartFile file) throws IOException;

    ResponseEntity<ByteArrayResource> fetchUserProfilePictureFromS3(String username);

    ResponseEntity<String> deleteProfilePictureFromS3(String username);
}
