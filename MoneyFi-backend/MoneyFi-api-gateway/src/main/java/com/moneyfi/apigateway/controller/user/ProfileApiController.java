package com.moneyfi.apigateway.controller.user;

import com.moneyfi.apigateway.exceptions.ResourceNotFoundException;
import com.moneyfi.apigateway.service.common.UserCommonService;
import com.moneyfi.apigateway.service.common.dto.request.UserDefectRequestDto;
import com.moneyfi.apigateway.service.common.dto.request.UserFeedbackRequestDto;
import com.moneyfi.apigateway.service.common.dto.response.ProfileDetailsDto;
import com.moneyfi.apigateway.service.common.dto.response.UserNotificationResponseDto;
import com.moneyfi.apigateway.service.userservice.dto.request.ChangePasswordDto;
import com.moneyfi.apigateway.model.common.ProfileModel;
import com.moneyfi.apigateway.service.common.ProfileService;
import com.moneyfi.apigateway.service.userservice.UserService;
import com.moneyfi.apigateway.service.userservice.dto.request.AccountBlockOrDeleteRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
public class ProfileApiController {

    private final ProfileService profileService;
    private final UserService userService;
    private final UserCommonService userCommonService;

    public ProfileApiController(ProfileService profileService,
                                UserService userService,
                                UserCommonService userCommonService){
        this.profileService = profileService;
        this.userService = userService;
        this.userCommonService = userCommonService;
    }

    @GetMapping("/test")
    public Long testFunction(Authentication authentication){
        if(authentication.isAuthenticated()){
            String username = ((UserDetails) authentication.getPrincipal()).getUsername();
            return  userService.getUserIdByUsername(username);
        }
        return null;
    }

    @Operation(summary = "Api to save/update the profile details of a user")
    @PostMapping("/profile-details/save")
    public ResponseEntity<ProfileDetailsDto> saveProfileDetails(Authentication authentication,
                                                                @RequestBody ProfileModel profile){
        Long userId = userService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        return ResponseEntity.ok(profileService.saveUserDetails(userId, profile));
    }

    @Operation(summary = "Api to get profile details of a user")
    @GetMapping("/profile-details/get")
    public ResponseEntity<ProfileDetailsDto> getProfileDetails(Authentication authentication){
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return ResponseEntity.ok(profileService.getProfileDetailsOfUser(username));
    }

    @Operation(summary = "Api to get the name of a user")
    @GetMapping("/name/get")
    public ResponseEntity<String> getNameOfUserByUserId(Authentication authentication){
        Long userId = userService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        return ResponseEntity.ok(profileService.getUserDetailsByUserId(userId));
    }

    @Operation(summary = "Api to save the user raised defect details")
    @PostMapping("/report-issue")
    public void saveUserRaisedReports(Authentication authentication,
                                      @Valid @ModelAttribute UserDefectRequestDto userDefectRequestDto){
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        profileService.saveContactUsDetails(userDefectRequestDto, userService.getUserIdByUsername(username), username);
    }

    @Operation(summary = "Api to save the user feedback details")
    @PostMapping("/submit-feedback")
    public void saveUserFeedback(@RequestBody @Valid UserFeedbackRequestDto feedback){
        profileService.saveFeedback(feedback);
    }

    @Operation(summary = "Api to get the user id from user's email")
    @GetMapping("/getUserId/{email}")
    public Long getUserIdByUserEmail(@PathVariable("email") String email){
        return userService.getUserIdByUsername(email);
    }

    @Operation(summary = "Api to get the username from token and simply return")
    @GetMapping("/get-username")
    public ResponseEntity<String> getUsername(Authentication authentication){
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return ResponseEntity.ok(username);
    }

    @Operation(summary = "Api to change password for logged in user")
    @PostMapping("/change-password")
    public void changePassword(Authentication authentication,
                                                @RequestBody ChangePasswordDto changePasswordDto) {
        Long userId = userService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        changePasswordDto.setUserId(userId);
        userService.changePassword(changePasswordDto);
    }

    @Operation(summary = "Api to send user's account statement as email")
    @PostMapping("/account-statement/email")
    public ResponseEntity<Void> sendAccountStatementEmailToUser(Authentication authentication,
                                                                @RequestBody byte[] pdfBytes) {
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        if(!userService.sendAccountStatementEmail(username, pdfBytes)){
            throw new ResourceNotFoundException("Error in sending email, internal error");
        }
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Api to send user's account spending analysis as email")
    @PostMapping("/spending-analysis/email")
    public ResponseEntity<Void> sendSpendingAnalysisEmailToUser(Authentication authentication,
                                                                @RequestBody byte[] pdfBytes) {
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        if(!userService.sendSpendingAnalysisEmail(username, pdfBytes)){
            throw new ResourceNotFoundException("Error in sending email, internal error");
        }
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Api to upload profile pic to AWS S3")
    @PostMapping("/profile-picture/upload")
    public ResponseEntity<String> uploadUserProfilePictureToS3(Authentication authentication,
                                                               @RequestParam(value = "file") MultipartFile file) throws IOException {
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return ResponseEntity.ok(userService.uploadUserProfilePictureToS3(username, file));
    }

    @Operation(summary = "Api to fetch the user profile picture from aws s3")
    @GetMapping("/profile-picture/get")
    public ResponseEntity<ByteArrayResource> fetchUserProfilePictureFromS3(Authentication authentication) {
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userService.fetchUserProfilePictureFromS3(username);
    }

    @Operation(summary = "Api to delete the user profile picture from aws s3")
    @DeleteMapping("/profile-picture/delete")
    public ResponseEntity<String> deleteProfilePictureFromS3(Authentication authentication) {
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userService.deleteProfilePictureFromS3(username);
    }

    @Operation(summary = "Api to get the admin scheduled notifications")
    @GetMapping("/notifications/get")
    public ResponseEntity<List<UserNotificationResponseDto>> getUserNotifications(Authentication authentication){
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return ResponseEntity.ok(userCommonService.getUserNotifications(username));
    }

    @Operation(summary = "Api to get the admin scheduled notifications count")
    @GetMapping("/notifications/count")
    public ResponseEntity<Integer> getUserNotificationsCount(Authentication authentication){
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return ResponseEntity.ok(userCommonService.getUserNotificationsCount(username));
    }

    @Operation(summary = "Api to update the seen status of the notification by user")
    @PutMapping("/notification/update")
    public void updateUserNotificationSeenStatus(Authentication authentication,
                                                 @RequestParam("ids") String notificationIds){
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        userCommonService.updateUserNotificationSeenStatus(username,notificationIds);
    }

    @Operation(summary = "Api to send otp to block/delete the account")
    @GetMapping("/otp-request/account-deactivate-actions")
    public ResponseEntity<String> sendOtpForBlockAndDeleteAccountByUserRequest(Authentication authentication,
                                                                               @RequestParam String type){
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userService.sendOtpToBlockAccount(username, type);
    }

    @Operation(summary = "Api to block/delete account based on user request")
    @PostMapping("/deactivate-account")
    public ResponseEntity<String> blockOrDeleteAccountByUserRequest(Authentication authentication,
                                                                    @RequestBody AccountBlockOrDeleteRequestDto request) {
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userService.blockOrDeleteAccountByUserRequest(username, request);
    }

    @Operation(summary = "Api to download the empty template of excel for user profile details to be saved")
    @GetMapping("/profile-details-template/download")
    public ResponseEntity<byte[]> downloadTemplateForUserProfile() {
        return profileService.downloadTemplateForUserProfile();
    }

    @Operation(summary = "Api to parse the excel to extract the user's profile data and save into db")
    @PostMapping("/user-profile/excel-upload")
    public ResponseEntity<String> parseUserProfileDataFromExcel(Authentication authentication,
                                                                @RequestParam("file") MultipartFile excel){
        Long userId = userService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        return profileService.parseUserProfileDataFromExcel(excel, userId);
    }

    @Operation(summary = "Api to get the reasons for the respected reason codes")
    @GetMapping("/reasons-dialog/get")
    public ResponseEntity<List<String>> getReasonsForDialogForUser(@RequestParam("code") int reasonCode){
        List<String> responseList = userCommonService.getReasonsForDialogForUser(reasonCode);
        return !responseList.isEmpty() ? ResponseEntity.status(HttpStatus.OK).body(responseList) :
                                             ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @Operation(summary = "Method to logout/making the token blacklist")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logoutUser(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(userService.logout(token));
    }
}
