package com.moneyfi.user.controller;

import com.moneyfi.user.config.JwtService;
import com.moneyfi.user.exceptions.ResourceNotFoundException;
import com.moneyfi.user.model.ProfileModel;
import com.moneyfi.user.service.common.CommonService;
import com.moneyfi.user.service.common.UserCommonService;
import com.moneyfi.user.service.common.dto.request.AccountBlockOrDeleteRequestDto;
import com.moneyfi.user.service.common.dto.request.UserDefectRequestDto;
import com.moneyfi.user.service.common.dto.request.UserFeedbackRequestDto;
import com.moneyfi.user.service.common.dto.response.UserNotificationResponseDto;
import com.moneyfi.user.service.profile.ProfileService;
import com.moneyfi.user.service.profile.dto.ProfileDetailsDto;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/user-service/user")
public class UserController {

    private final JwtService jwtService;
    private final ProfileService profileService;
    private final CommonService commonService;
    private final UserCommonService userCommonService;

    public UserController(JwtService jwtService,
                          ProfileService profileService,
                          CommonService commonService,
                          UserCommonService userCommonService){
        this.jwtService = jwtService;
        this.profileService = profileService;
        this.commonService = commonService;
        this.userCommonService = userCommonService;
    }

    @Operation(summary = "Api end point to test")
    @GetMapping("/test")
    public String testFunction(){
        return "method entered";
    }

    @Operation(summary = "Api to save/update the profile details of a user")
    @PostMapping("/profile-details/save")
    public ResponseEntity<ProfileDetailsDto> saveProfileDetails(@RequestHeader("Authorization") String authHeader,
                                                                @RequestBody ProfileModel profile){
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return ResponseEntity.ok(profileService.saveUserDetails(userId, profile));
    }

    @Operation(summary = "Api to get profile details of a user")
    @GetMapping("/profile-details/get")
    public ResponseEntity<ProfileDetailsDto> getProfileDetails(@RequestHeader("Authorization") String authHeader){
        String username = jwtService.extractUsernameFromToken(authHeader.substring(7));
        return ResponseEntity.ok(profileService.getProfileDetailsOfUser(username));
    }

    @Operation(summary = "Api to get the name of a user")
    @GetMapping("/name/get")
    public ResponseEntity<String> getNameOfUserByUserId(@RequestHeader("Authorization") String authHeader){
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return ResponseEntity.ok(profileService.getUserDetailsByUserId(userId));
    }

    @Operation(summary = "Api to save the user raised defect details")
    @PostMapping("/report-issue")
    public void saveUserRaisedReports(@RequestHeader("Authorization") String authHeader,
                                      @Valid @ModelAttribute UserDefectRequestDto userDefectRequestDto){
        String username = jwtService.extractUsernameFromToken(authHeader.substring(7));
        profileService.saveContactUsDetails(userDefectRequestDto, jwtService.extractUserIdFromToken(authHeader.substring(7)), username);
    }

    @Operation(summary = "Api to save the user feedback details")
    @PostMapping("/submit-feedback")
    public void saveUserFeedback(@RequestBody @Valid UserFeedbackRequestDto feedback){
        profileService.saveFeedback(feedback);
    }

    @Operation(summary = "Api to send user's account statement as email")
    @PostMapping("/account-statement/email")
    public ResponseEntity<Void> sendAccountStatementEmailToUser(@RequestHeader("Authorization") String authHeader,
                                                                @RequestBody byte[] pdfBytes) {
        String username = jwtService.extractUsernameFromToken(authHeader.substring(7));
        if(!commonService.sendAccountStatementEmail(username, jwtService.extractUserIdFromToken(authHeader.substring(7)), pdfBytes)){
            throw new ResourceNotFoundException("Error in sending email, internal error");
        }
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Api to send user's account spending analysis as email")
    @PostMapping("/spending-analysis/email")
    public ResponseEntity<Void> sendSpendingAnalysisEmailToUser(@RequestHeader("Authorization") String authHeader,
                                                                @RequestBody byte[] pdfBytes) {
        String username = jwtService.extractUsernameFromToken(authHeader.substring(7));
        if(!commonService.sendSpendingAnalysisEmail(username, jwtService.extractUserIdFromToken(authHeader.substring(7)), pdfBytes)){
            throw new ResourceNotFoundException("Error in sending email, internal error");
        }
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Api to upload profile pic to AWS S3")
    @PostMapping("/profile-picture/upload")
    public ResponseEntity<String> uploadUserProfilePictureToS3(@RequestHeader("Authorization") String authHeader,
                                                               @RequestParam(value = "file") MultipartFile file) throws IOException {
        String username = jwtService.extractUsernameFromToken(authHeader.substring(7));
        return ResponseEntity.ok(userCommonService.uploadUserProfilePictureToS3(username, jwtService.extractUserIdFromToken(authHeader.substring(7)), file));
    }

    @Operation(summary = "Api to fetch the user profile picture from aws s3")
    @GetMapping("/profile-picture/get")
    public ResponseEntity<ByteArrayResource> fetchUserProfilePictureFromS3(@RequestHeader("Authorization") String authHeader) {
        String username = jwtService.extractUsernameFromToken(authHeader.substring(7));
        return userCommonService.fetchUserProfilePictureFromS3(username, jwtService.extractUserIdFromToken(authHeader.substring(7)));
    }

    @Operation(summary = "Api to delete the user profile picture from aws s3")
    @DeleteMapping("/profile-picture/delete")
    public ResponseEntity<String> deleteProfilePictureFromS3(@RequestHeader("Authorization") String authHeader) {
        String username = jwtService.extractUsernameFromToken(authHeader.substring(7));
        return userCommonService.deleteProfilePictureFromS3(username, jwtService.extractUserIdFromToken(authHeader.substring(7)));
    }

    @Operation(summary = "Api to get the admin scheduled notifications")
    @GetMapping("/notifications/get")
    public ResponseEntity<List<UserNotificationResponseDto>> getUserNotifications(@RequestHeader("Authorization") String authHeader){
        String username = jwtService.extractUsernameFromToken(authHeader.substring(7));
        return ResponseEntity.ok(userCommonService.getUserNotifications(username));
    }

    @Operation(summary = "Api to get the admin scheduled notifications count")
    @GetMapping("/notifications/count")
    public ResponseEntity<Integer> getUserNotificationsCount(@RequestHeader("Authorization") String authHeader){
        String username = jwtService.extractUsernameFromToken(authHeader.substring(7));
        return ResponseEntity.ok(userCommonService.getUserNotificationsCount(username));
    }

    @Operation(summary = "Api to update the seen status of the notification by user")
    @PutMapping("/notification/update")
    public void updateUserNotificationSeenStatus(@RequestHeader("Authorization") String authHeader,
                                                 @RequestParam("ids") String notificationIds){
        String username = jwtService.extractUsernameFromToken(authHeader.substring(7));
        userCommonService.updateUserNotificationSeenStatus(username, notificationIds);
    }

    @Operation(summary = "Api to download the empty template of excel for user profile details to be saved")
    @GetMapping("/profile-details-template/download")
    public ResponseEntity<byte[]> downloadTemplateForUserProfile() {
        return profileService.downloadTemplateForUserProfile();
    }

    @Operation(summary = "Api to parse the excel to extract user's profile data and save into db")
    @PostMapping("/user-profile/excel-upload")
    public void parseUserProfileDataFromExcel(@RequestHeader("Authorization") String authHeader,
                                              @RequestParam("file") MultipartFile excel){
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        profileService.parseUserProfileDataFromExcel(excel, userId);
    }

    @Operation(summary = "Api to block/delete account based on user request")
    @PostMapping("/deactivate-account")
    public ResponseEntity<String> blockOrDeleteAccountByUserRequest(@RequestHeader("Authorization") String authHeader,
                                                                    @RequestBody AccountBlockOrDeleteRequestDto request) {
        String username = jwtService.extractUsernameFromToken(authHeader.substring(7));
        return userCommonService.blockOrDeleteAccountByUserRequest(username, request);
    }
}
