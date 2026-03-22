package com.moneyfi.user.controller.open;

import com.moneyfi.constants.enums.LoginMode;
import com.moneyfi.user.service.user.UserAuthService;
import com.moneyfi.user.service.user.UserCommonService;
import com.moneyfi.user.service.user.dto.request.UserLoginDetailsRequestDto;
import com.moneyfi.user.service.user.dto.request.UserProfile;
import com.moneyfi.user.service.user.dto.response.RemainingTimeCountDto;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/user-service/auth")
@RequiredArgsConstructor
@Validated
public class UserAuthController {

    private final UserAuthService userAuthService;
    private final UserCommonService userCommonService;

    @Operation(summary = "Api for user registration/signup using email password mode")
    @PostMapping("/register")
    public void registerUser(@Valid @RequestBody UserProfile userProfile) {
        userAuthService.registerUser(userProfile, LoginMode.EMAIL_PASSWORD.name(), null);
    }

    @Operation(summary = "Api to send Otp for user verification during signup")
    @GetMapping("/send-otp/signup")
    public ResponseEntity<String> sendOtpForSignup(@NotBlank @Email @RequestParam(value = "email") String email,
                                                   @NotBlank @RequestParam(value = "name") String name){
        return ResponseEntity.ok(userAuthService.sendOtpForSignup(email, name));
    }

    @Operation(summary = "Api to verify otp during user creation")
    @GetMapping("/{email}/{inputOtp}/check-otp/signup")
    public ResponseEntity<Boolean> checkEnteredOtp(@NotBlank @Email @PathVariable("email") String email,
                                                   @NotBlank @PathVariable("inputOtp") String inputOtp){
        return ResponseEntity.ok(userAuthService.checkEnteredOtpDuringSignup(email, inputOtp));
    }

    @Operation(summary = "Api end point for user to login")
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginUserViaEmailPasswordMode(@Valid @RequestBody UserLoginDetailsRequestDto requestDto) {
        try {
            return userAuthService.login(requestDto);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Api to check the eligibility for next otp")
    @GetMapping("/{email}/otp-send/check")
    public ResponseEntity<RemainingTimeCountDto> checkOtpActiveMethod(@NotBlank @Email @PathVariable("email") String email){
        return ResponseEntity.ok(userAuthService.checkOtpActiveMethod(email));
    }

    @Operation(summary = "Api end point to send otp for forgot password")
    @GetMapping("/forgot-password/get-otp")
    public ResponseEntity<String> forgotPassword(@NotBlank @Email @RequestParam String email) {
        return ResponseEntity.ok(userAuthService.forgotPassword(email));
    }

    @Operation(summary = "Api end point for verification of code/otp during forgot password process")
    @GetMapping("/forgot-password/verify-otp")
    public ResponseEntity<String> verifyCode(@NotBlank @Email @RequestParam String email,
                                             @NotBlank @RequestParam String code) {
        return ResponseEntity.ok(userAuthService.verifyCode(email, code));
    }

    @Operation(summary = "Api end point to update the user's password for forgot password")
    @PutMapping("/forgot-password/update-password")
    public ResponseEntity<String> updatePassword(@NotBlank @Email @RequestParam String email,
                                                 @NotBlank @RequestParam String password){
        return ResponseEntity.ok(userAuthService.updatePasswordOnUserForgotMode(email, password));
    }

    @Operation(summary = "Api to resend otp")
    @GetMapping("/otp-resend")
    public void resendOtp(@NotBlank @Email @RequestParam(value = "username") String username,
                          @NotBlank @RequestParam(value = "type") String otpType) {
        userAuthService.resendOtp(username, otpType);
    }
}
