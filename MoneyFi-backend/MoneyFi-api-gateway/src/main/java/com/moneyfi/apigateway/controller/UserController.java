package com.moneyfi.apigateway.controller;

import com.moneyfi.apigateway.dto.*;
import com.moneyfi.apigateway.model.BlackListedToken;
import com.moneyfi.apigateway.repository.UserRepository;
import com.moneyfi.apigateway.model.User;
import com.moneyfi.apigateway.service.*;
import com.moneyfi.apigateway.service.jwtservice.JwtService;
import com.moneyfi.apigateway.service.resetpassword.ResetPassword;
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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin("http://localhost:4200")
@RequestMapping("/auth")
public class UserController {

    @Autowired
    private AuthenticationManager authenticationManager;

    private final UserService userService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ResetPassword passwordResetService;
    private final TokenBlacklistService blacklistService;

    public UserController(UserService userService,
                          JwtService jwtService,
                          UserRepository userRepository,
                          ResetPassword resetPassword,
                          TokenBlacklistService blacklistService){
        this.userService = userService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordResetService = resetPassword;
        this.blacklistService = blacklistService;
    }


    @Operation(summary = "Method to get the user id from user's email")
    @GetMapping("/getUserId/{email}")
    public Integer getUserId(@PathVariable("email") String email){
        User user = userRepository.findByUsername(email);
        return Math.toIntExact(user.getId());
    }

    @Operation(summary = "Method for the user registration/signup")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserProfile userProfile) {
        User user = new User();
        user.setUsername(userProfile.getUsername());
        user.setPassword(userProfile.getPassword());

        User getUser = userRepository.findByUsername(user.getUsername());
        if (getUser != null) {
            // Return a conflict status code with a custom message when user already exists
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists");
        }

        // If user doesn't exist, proceed with saving the user
        User savedUser = userService.saveUser(user);
        return ResponseEntity.ok(jwtService.generateToken(user.getUsername()));
    }

    @Operation(summary = "Method for the user login")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        try {
            // Validate user input (username and password should not be empty)
            if (user.getUsername() == null ||
                    user.getUsername().isEmpty() ||
                    user.getPassword() == null ||
                    user.getPassword().isEmpty()) {

                return ResponseEntity.badRequest().body("Username and password are required");
            }

            // Check if the user exists in the database
            User existingUser = userRepository.findByUsername(user.getUsername());
            if (existingUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found. Please sign up.");
            }

            try {
                // Authenticate the user with the provided password
                Authentication authentication = authenticationManager
                        .authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));

                // If authentication is successful
                if (authentication.isAuthenticated()) {
                    JwtToken token = jwtService.generateToken(user.getUsername());
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


    @Operation(summary = "Method to get user id from token")
    @GetMapping("/token/{token}")
    public Integer getUserIdFromToken(@PathVariable("token") String token){
        String username = jwtService.extractUserName(token);
        Long id = userRepository.findByUsername(username).getId();
        return Math.toIntExact(id);
    }

    @Operation(summary = "Method for password forgot")
    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email) {
        return passwordResetService.forgotPassword(email);
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

    @GetMapping("/contactUsEmail/{message}/{images}")
    public boolean sendContactUsEmailOfUser(@PathVariable("message") String message,
                                            @PathVariable("images") String images){
        return userService.sendContactUsEmailOfUser(message, images);
    }

    @GetMapping("/feedbackEmail/{rating}/{comment}")
    public boolean sendUserFeedBackEmail(@PathVariable("rating") int rating,
                                         @PathVariable("comment") String comment){
        return userService.sendUserFeedBackEmail(rating, comment);
    }
}
