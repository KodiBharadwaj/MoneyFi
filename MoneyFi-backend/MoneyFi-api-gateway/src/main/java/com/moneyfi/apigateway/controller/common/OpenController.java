package com.moneyfi.apigateway.controller.common;

import com.moneyfi.apigateway.service.common.UserCommonService;
import com.moneyfi.apigateway.service.userservice.UserService;
import com.moneyfi.apigateway.service.userservice.dto.request.UserLoginDetailsRequestDto;
import com.moneyfi.apigateway.service.userservice.dto.response.RemainingTimeCountDto;
import com.moneyfi.apigateway.service.userservice.dto.request.UserProfile;
import com.moneyfi.apigateway.util.enums.LoginMode;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class OpenController {

    private final UserService userService;
    private final UserCommonService userCommonService;

    public OpenController(UserService userService,
                          UserCommonService resetPassword){
        this.userService = userService;
        this.userCommonService = resetPassword;
    }

    @Operation(summary = "Api end point to test")
    @GetMapping("/test")
    public String testFunction(){
        return "method entered";
    }

    @Operation(summary = "Api end point for the user registration/signup using email password mode")
    @PostMapping("/register")
    public void registerUser(@Valid @RequestBody UserProfile userProfile) {
        userService.registerUser(userProfile, LoginMode.EMAIL_PASSWORD.name(), null);
    }

    @Operation(summary = "Api to send Otp for user verification during signup")
    @GetMapping("/send-otp/signup")
    public ResponseEntity<String> sendOtpForSignup(@RequestParam("email") String email,
                                                   @RequestParam("name") String name){

        return ResponseEntity.ok(userService.sendOtpForSignup(email, name));
    }

    @Operation(summary = "Api to check entered otp is correct or not during user creation")
    @GetMapping("/{email}/{inputOtp}/check-otp/signup")
    public ResponseEntity<Boolean> checkEnteredOtp(@PathVariable("email") String email,
                                   @PathVariable("inputOtp") String inputOtp){
        return ResponseEntity.ok(userService.checkEnteredOtpDuringSignup(email, inputOtp));
    }

    @Operation(summary = "Api end point for user to login")
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginUserViaEmailPasswordMode(@Valid @RequestBody UserLoginDetailsRequestDto requestDto) {
        try {
            return userService.login(requestDto);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Api end point to send otp for forgot password")
    @GetMapping("/forgot-password/get-otp")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        return ResponseEntity.ok(userCommonService.forgotPassword(email));
    }

    @Operation(summary = "Api end point for verification of code/otp during forgot password process")
    @GetMapping("/forgot-password/verify-otp")
    public ResponseEntity<String> verifyCode(@RequestParam String email,
                                             @RequestParam String code) {
        return ResponseEntity.ok(userCommonService.verifyCode(email, code));
    }

    @Operation(summary = "Api end point to update the user's password for forgot password")
    @PutMapping("/forgot-password/update-password")
    public ResponseEntity<String> updatePassword(@RequestParam String email,
                                                 @RequestParam String password){
        return ResponseEntity.ok(userCommonService.updatePasswordOnUserForgotMode(email, password));
    }

    @Operation(summary = "Api to check the eligibility for next otp")
    @GetMapping("/{email}/otp-send/check")
    public ResponseEntity<RemainingTimeCountDto> checkOtpActiveMethod(@PathVariable("email") String email){
        return ResponseEntity.ok(userService.checkOtpActiveMethod(email));
    }
}
