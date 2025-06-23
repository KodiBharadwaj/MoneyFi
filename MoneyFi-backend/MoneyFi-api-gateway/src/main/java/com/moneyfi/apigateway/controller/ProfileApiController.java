package com.moneyfi.apigateway.controller;

import com.moneyfi.apigateway.service.common.dto.ProfileDetailsDto;
import com.moneyfi.apigateway.service.userservice.dto.ChangePasswordDto;
import com.moneyfi.apigateway.service.userservice.dto.ProfileChangePassword;
import com.moneyfi.apigateway.model.common.ContactUs;
import com.moneyfi.apigateway.model.common.Feedback;
import com.moneyfi.apigateway.model.common.ProfileModel;
import com.moneyfi.apigateway.service.common.ProfileService;
import com.moneyfi.apigateway.service.userservice.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/userProfile")
public class ProfileApiController {

    private final ProfileService profileService;
    private final UserService userService;

    public ProfileApiController(ProfileService profileService,
                                UserService userService){
        this.profileService = profileService;
        this.userService = userService;
    }

    @GetMapping("/test")
    public Long testFunction(Authentication authentication){
        if(authentication.isAuthenticated()){
            String username = ((UserDetails) authentication.getPrincipal()).getUsername();
            return  userService.getUserIdByUsername(username);
        }
        return null;
    }

    @Operation(summary = "Method to save/update the profile details of a user")
    @PostMapping("saveProfile")
    public ResponseEntity<ProfileDetailsDto> saveProfile(Authentication authentication,
                                                         @RequestBody ProfileModel profile){
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long userId = userService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        return ResponseEntity.ok(profileService.saveUserDetails(userId, profile));
    }

    @Operation(summary = "Method to get the profile details of a user")
    @GetMapping("/getProfile")
    public ResponseEntity<ProfileDetailsDto> getProfile(Authentication authentication){
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = userService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        ProfileDetailsDto profileDetails = profileService.getProfileDetailsOfUser(userId);

        if (profileDetails != null) {
            return ResponseEntity.ok(profileDetails);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Operation(summary = "Method to get the name of a user")
    @GetMapping("/getName")
    public ResponseEntity<String> getNameFromUserId(Authentication authentication){
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = userService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        ProfileModel profile = profileService.getUserDetailsByUserId(userId);
        if (profile != null) {
            return ResponseEntity.ok(profile.getName());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Operation(summary = "Method which deals with user contact/report details")
    @PostMapping("/contactUs")
    public ResponseEntity<ContactUs> saveContactUsDetails(Authentication authentication,
                                                          @RequestBody ContactUs contactUsDetails){
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long userId = userService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        contactUsDetails.setUserId(userId);
        return ResponseEntity.ok(profileService.saveContactUsDetails(contactUsDetails));
    }

    @Operation(summary = "Method which deals with user feedback")
    @PostMapping("/feedback")
    public ResponseEntity<Feedback> saveFeedback(Authentication authentication,
                                                 @RequestBody Feedback feedback){
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long userId = userService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        feedback.setUserId(userId);
        return ResponseEntity.ok(profileService.saveFeedback(feedback));
    }

    @Operation(summary = "Method to get the user id from user's email")
    @GetMapping("/getUserId/{email}")
    public Long getUserId(@PathVariable("email") String email){
        return userService.getUserIdByUsername(email);
    }

    @Operation(summary = "Method to change the password for the logged in user in the profile section")
    @PostMapping("/change-password")
    public ProfileChangePassword changePassword(Authentication authentication,
                                                @RequestBody ChangePasswordDto changePasswordDto) {
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        Long userId = userService.getUserIdByUsername(username);
        changePasswordDto.setUserId(userId);
        return userService.changePassword(changePasswordDto);
    }

    @Operation(summary = "API to send user's account statement as email")
    @PostMapping("/account-statement/email")
    public ResponseEntity<Void> sendAccountStatementEmail(Authentication authentication,
                                                          @RequestBody byte[] pdfBytes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        userService.sendAccountStatementEmail(username, pdfBytes);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Api to upload profile pic to aws s3")
    @PostMapping("/upload/profile-picture")
    public ResponseEntity<String> uploadUserProfilePictureToS3(Authentication authentication,
                                                           @RequestParam(value = "file") MultipartFile file) throws IOException {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return ResponseEntity.ok(userService.uploadUserProfilePictureToS3(username, file));
    }

    @Operation(summary = "Api to fetch the user profile picture from aws s3")
    @GetMapping("/fetch/profile-picture")
    public ResponseEntity<ByteArrayResource> fetchUserProfilePictureFromS3(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userService.fetchUserProfilePictureFromS3(username);
    }

    @Operation(summary = "Api to delete the user profile picture from aws s3")
    @DeleteMapping("/delete/profile-picture")
    public ResponseEntity<String> deleteProfilePictureFromS3(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userService.deleteProfilePictureFromS3(username);
    }

    @Operation(summary = "Method to logout/making the token blacklist")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logoutUser(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(userService.logout(token));
    }
}
