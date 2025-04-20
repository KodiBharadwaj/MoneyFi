package com.moneyfi.apigateway.controller;

import com.moneyfi.apigateway.dto.*;
import com.moneyfi.apigateway.model.auth.BlackListedToken;
import com.moneyfi.apigateway.model.auth.SessionTokenModel;
import com.moneyfi.apigateway.model.auth.UserAuthModel;
import com.moneyfi.apigateway.repository.auth.UserRepository;
import com.moneyfi.apigateway.service.*;
import com.moneyfi.apigateway.service.jwtservice.JwtService;
import com.moneyfi.apigateway.service.resetpassword.ResetPassword;
import com.moneyfi.apigateway.service.sessiontokens.SessionToken;
import com.moneyfi.apigateway.service.userservice.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin("http://localhost:4200")
@RequestMapping("/api/auth")
public class UserController {

    @Autowired
    private AuthenticationManager authenticationManager;

    private final UserService userService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ResetPassword passwordResetService;
    private final TokenBlacklistService blacklistService;
    private final SessionToken sessionTokenService;


    public UserController(UserService userService,
                          JwtService jwtService,
                          UserRepository userRepository,
                          ResetPassword resetPassword,
                          TokenBlacklistService blacklistService,
                          SessionToken sessionToken){
        this.userService = userService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordResetService = resetPassword;
        this.blacklistService = blacklistService;
        this.sessionTokenService = sessionToken;
    }


    @Operation(summary = "Method for the user registration/signup")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserProfile userProfile) {

        UserAuthModel user = userService.registerUser(userProfile);
        if(user == null){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists"); //409
        } else {
            return ResponseEntity.ok(jwtService.generateToken(userProfile.getUsername())); // 200
        }
    }

    @Operation(summary = "Method for the user to login")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserAuthModel userAuthModel) {
        SessionTokenModel sessionTokenUser = sessionTokenService.getUserByUsername(userAuthModel.getUsername());
        if(sessionTokenUser != null){
            if(sessionTokenUser.getIsActive()){
                String oldToken = sessionTokenUser.getToken();

                BlackListedToken blackListedToken = new BlackListedToken();
                blackListedToken.setToken(oldToken);
                Date expiryDate = new Date(System.currentTimeMillis() + 3600000);
                blackListedToken.setExpiry(expiryDate);
                blacklistService.blacklistToken(blackListedToken);
            }
        }
        try {
            // Validate user input (username and password should not be empty)
            if (userAuthModel.getUsername() == null ||
                    userAuthModel.getUsername().isEmpty() ||
                    userAuthModel.getPassword() == null ||
                    userAuthModel.getPassword().isEmpty()) {

                return ResponseEntity.badRequest().body("Username and password are required");
            }

            // Check if the user exists in the database
            UserAuthModel existingUser = userRepository.findByUsername(userAuthModel.getUsername());
            if (existingUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("UserAuthModel not found. Please sign up.");
            }

            try {
                // Authenticate the user with the provided password
                Authentication authentication = authenticationManager
                        .authenticate(new UsernamePasswordAuthenticationToken(userAuthModel.getUsername(), userAuthModel.getPassword()));

                // If authentication is successful
                if (authentication.isAuthenticated()) {
                    JwtToken token = jwtService.generateToken(userAuthModel.getUsername());

                    // Conditions to store the jwt token to prevent multiple logins of same account in different browsers
                    if(sessionTokenService.getUserByUsername(userAuthModel.getUsername()) != null){
                        SessionTokenModel sessionTokens = sessionTokenService.getUserByUsername(userAuthModel.getUsername());
                        sessionTokens.setUsername(userAuthModel.getUsername());
                        sessionTokens.setCreatedTime(LocalDateTime.now());
                        sessionTokens.setToken(token.getJwtToken());
                        sessionTokens.setIsActive(true);
                        sessionTokenService.save(sessionTokens);
                    } else {
                        SessionTokenModel sessionTokens = new SessionTokenModel();
                        sessionTokens.setUsername(userAuthModel.getUsername());
                        sessionTokens.setCreatedTime(LocalDateTime.now());
                        sessionTokens.setToken(token.getJwtToken());
                        sessionTokens.setIsActive(true);
                        sessionTokenService.save(sessionTokens);
                    }

                    return ResponseEntity.ok(token);
                }
            } catch (BadCredentialsException ex) {
                // If the password is incorrect
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect password");
            }

            // Default case for any other authentication failures
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        } catch (Exception e) {
            // Handle any unexpected errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during login");
        }
    }

    @Operation(summary = "Method to get the user id from user's email")
    @GetMapping("/getUserId/{email}")
    public Long getUserId(@PathVariable("email") String email){
        return userService.getUserIdByUsername(email);
    }

    @Operation(summary = "Method to get user id from token")
    @GetMapping("/token/{token}")
    public Long getUserIdFromToken(@PathVariable("token") String token){
        return userService.getUserIdFromToken(token);
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
            throw new RuntimeException("Invalid verification code");
        }
    }

    @Operation(summary = "Method to update the user's password")
    @PutMapping("/update-password")
    public String updatePassword(@RequestParam String email,@RequestParam String password)
    {
        return passwordResetService.UpdatePassword(email,password);
    }


    @Operation(summary = "Method to logout/making the token blacklist")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logoutUser(@RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Date expiryDate = new Date(System.currentTimeMillis() + 3600000); // Expiry 1 hour later
        BlackListedToken blackListedToken = new BlackListedToken();
        blackListedToken.setToken(token);
        blackListedToken.setExpiry(expiryDate);
        blacklistService.blacklistToken(blackListedToken);

        SessionTokenModel sessionTokens = sessionTokenService.getSessionDetailsByToken(token);
        sessionTokens.setIsActive(false);
        sessionTokenService.save(sessionTokens);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Method to change the password for the logged in user in the profile section")
    @PostMapping("/change-password")
    public ProfileChangePassword changePassword(@RequestBody ChangePasswordDto changePasswordDto) {
        return userService.changePassword(changePasswordDto);
    }

    @Operation(summary = "Method to check the eligibility for next otp")
    @GetMapping("/checkOtpActive/{email}")
    public RemainingTimeCountDto checkOtpActiveMethod(@PathVariable("email") String email){
        return userService.checkOtpActiveMethod(email);
    }

    @Operation(summary = "Method to send Otp for user verification during signup")
    @GetMapping("/sendOtpForSignup/{email}/{name}")
    public ResponseEntity<String> sendOtpForSignup(@PathVariable("email") String email,
                                    @PathVariable("name") String name){

        return ResponseEntity.ok(userService.sendOtpForSignup(email, name));
    }

    @Operation(summary = "Method to check the otp entered correct or not during user creation")
    @GetMapping("/checkOtp/{email}/{inputOtp}")
    public boolean checkEnteredOtp(@PathVariable("email") String email,
                                   @PathVariable("inputOtp") String inputOtp){
        return userService.checkEnteredOtp(email, inputOtp);
    }
}
