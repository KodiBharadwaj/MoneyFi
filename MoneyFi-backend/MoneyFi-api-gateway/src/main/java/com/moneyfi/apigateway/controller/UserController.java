package com.moneyfi.apigateway.controller;

import com.moneyfi.apigateway.model.auth.UserAuthModel;
import com.moneyfi.apigateway.service.jwtservice.JwtService;
import com.moneyfi.apigateway.service.common.UserCommonRepository;
import com.moneyfi.apigateway.service.userservice.UserServiceRepository;
import com.moneyfi.apigateway.service.userservice.dto.ForgotUsernameDto;
import com.moneyfi.apigateway.service.userservice.dto.RemainingTimeCountDto;
import com.moneyfi.apigateway.service.userservice.dto.UserProfile;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("http://localhost:4200")
@RequestMapping("/api/auth")
public class UserController {

    private final UserServiceRepository userServiceRepository;
    private final JwtService jwtService;
    private final UserCommonRepository passwordResetService;


    public UserController(UserServiceRepository userServiceRepository,
                          JwtService jwtService,
                          UserCommonRepository resetPassword){
        this.userServiceRepository = userServiceRepository;
        this.jwtService = jwtService;
        this.passwordResetService = resetPassword;
    }


    @Operation(summary = "Method for the user registration/signup")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserProfile userProfile) {

        UserAuthModel user = userServiceRepository.registerUser(userProfile);
        if(user == null){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists"); //409
        } else {
            return ResponseEntity.ok(jwtService.generateToken(userProfile.getUsername())); // 200
        }
    }

    @Operation(summary = "Method for the user to login")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserAuthModel userAuthModel) {
        return userServiceRepository.login(userAuthModel);
    }


    @Operation(summary = "Method for password forgot")
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        return ResponseEntity.ok(passwordResetService.forgotPassword(email));
    }

    @Operation(summary = "Method for verification of code/otp")
    @PostMapping("/verify-code")
    public String verifyCode(@RequestParam String email, @RequestParam String code) {
        boolean isValid = passwordResetService.verifyCode(email, code);
        if (isValid) {
            return "Verification successful!";
        } else {
            throw new IllegalArgumentException("Invalid verification code");
        }
    }

    @Operation(summary = "Method to update the user's password")
    @PutMapping("/update-password")
    public String updatePassword(@RequestParam String email,@RequestParam String password)
    {
        return passwordResetService.UpdatePassword(email,password);
    }


    @Operation(summary = "Method to send Otp for user verification during signup")
    @GetMapping("/sendOtpForSignup/{email}/{name}")
    public ResponseEntity<String> sendOtpForSignup(@PathVariable("email") String email,
                                    @PathVariable("name") String name){

        return ResponseEntity.ok(userServiceRepository.sendOtpForSignup(email, name));
    }

    @Operation(summary = "Method to check the eligibility for next otp")
    @GetMapping("/checkOtpActive/{email}")
    public RemainingTimeCountDto checkOtpActiveMethod(@PathVariable("email") String email){
        return userServiceRepository.checkOtpActiveMethod(email);
    }

    @Operation(summary = "Method to check the otp entered correct or not during user creation")
    @GetMapping("/checkOtp/{email}/{inputOtp}")
    public boolean checkEnteredOtp(@PathVariable("email") String email,
                                   @PathVariable("inputOtp") String inputOtp){
        return userServiceRepository.checkEnteredOtp(email, inputOtp);
    }

    @Operation(summary = "Method to return username when user forgets username")
    @PostMapping("/forgotUsername")
    public boolean forgotUsername(@RequestBody ForgotUsernameDto userDetails){
        return userServiceRepository.getUsernameByDetails(userDetails);
    }
}
