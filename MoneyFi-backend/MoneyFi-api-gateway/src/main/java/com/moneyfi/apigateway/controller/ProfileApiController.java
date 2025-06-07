package com.moneyfi.apigateway.controller;

import com.moneyfi.apigateway.service.userservice.dto.ChangePasswordDto;
import com.moneyfi.apigateway.service.userservice.dto.ProfileChangePassword;
import com.moneyfi.apigateway.model.common.ContactUs;
import com.moneyfi.apigateway.model.common.Feedback;
import com.moneyfi.apigateway.model.common.ProfileModel;
import com.moneyfi.apigateway.service.common.ProfileServiceRepository;
import com.moneyfi.apigateway.service.userservice.UserServiceRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/userProfile")
public class ProfileApiController {

    private final ProfileServiceRepository profileServiceRepository;
    private final UserServiceRepository userServiceRepository;

    public ProfileApiController(ProfileServiceRepository profileServiceRepository,
                                UserServiceRepository userServiceRepository){
        this.profileServiceRepository = profileServiceRepository;
        this.userServiceRepository = userServiceRepository;
    }

    @GetMapping("/test")
    public Long testFunction(Authentication authentication){
        if(authentication.isAuthenticated()){
            String username = ((UserDetails) authentication.getPrincipal()).getUsername();
            return  userServiceRepository.getUserIdByUsername(username);
        }

        return null;
    }

    @Operation(summary = "Method to save/update the profile details of a user")
    @PostMapping("saveProfile")
    public ResponseEntity<ProfileModel> saveProfile(Authentication authentication,
                                    @RequestBody ProfileModel profile){

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            String username = userDetails.getUsername();
            Long userId = userServiceRepository.getUserIdByUsername(username);

            return ResponseEntity.ok(profileServiceRepository.saveUserDetails(userId, profile));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @Operation(summary = "Method to get the profile details of a user")
    @GetMapping("/getProfile")
    public ResponseEntity<ProfileModel> getProfile(Authentication authentication){

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            String username = userDetails.getUsername();
            Long userId = userServiceRepository.getUserIdByUsername(username);
            ProfileModel profile = profileServiceRepository.getUserDetailsByUserId(userId);

            if (profile != null) {
                return ResponseEntity.ok(profile);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @Operation(summary = "Method to get the name of a user")
    @GetMapping("/getName")
    public ResponseEntity<String> getNameFromUserId(Authentication authentication){
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            String username = userDetails.getUsername();
            Long userId = userServiceRepository.getUserIdByUsername(username);
            ProfileModel profile = profileServiceRepository.getUserDetailsByUserId(userId);

            if (profile != null) {
                return ResponseEntity.ok(profile.getName());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @Operation(summary = "Method which deals with user contact/report details")
    @PostMapping("/contactUs")
    public ResponseEntity<ContactUs> saveContactUsDetails(@RequestBody ContactUs contactUsDetails,
                                          Authentication authentication){
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            String username = userDetails.getUsername();
            Long userId = userServiceRepository.getUserIdByUsername(username);
            contactUsDetails.setUserId(userId);

            return ResponseEntity.ok(profileServiceRepository.saveContactUsDetails(contactUsDetails));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @Operation(summary = "Method which deals with user feedback")
    @PostMapping("/feedback")
    public ResponseEntity<Feedback> saveFeedback(Authentication authentication,
                                 @RequestBody Feedback feedback){

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            String username = userDetails.getUsername();
            Long userId = userServiceRepository.getUserIdByUsername(username);
            feedback.setUserId(userId);

            return ResponseEntity.ok(profileServiceRepository.saveFeedback(feedback));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @Operation(summary = "Method to get the user id from user's email")
    @GetMapping("/getUserId/{email}")
    public Long getUserId(@PathVariable("email") String email){
        return userServiceRepository.getUserIdByUsername(email);
    }

    @Operation(summary = "Method to change the password for the logged in user in the profile section")
    @PostMapping("/change-password")
    public ProfileChangePassword changePassword(Authentication authentication,
                                                @RequestBody ChangePasswordDto changePasswordDto) {
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        Long userId = userServiceRepository.getUserIdByUsername(username);
        changePasswordDto.setUserId(userId);
        return userServiceRepository.changePassword(changePasswordDto);
    }

    @Operation(summary = "Method to logout/making the token blacklist")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logoutUser(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(userServiceRepository.logout(token));
    }
}
