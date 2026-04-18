package com.moneyfi.user.controller.user;

import com.moneyfi.user.exceptions.ResourceNotFoundException;
import com.moneyfi.user.model.general.ProfileModel;
import com.moneyfi.user.service.user.CommonService;
import com.moneyfi.user.service.user.UserAuthService;
import com.moneyfi.user.service.user.UserCommonService;
import com.moneyfi.user.service.user.dto.request.*;
import com.moneyfi.user.service.user.dto.response.UserNotificationResponseDto;
import com.moneyfi.user.service.user.ProfileService;
import com.moneyfi.user.service.user.dto.response.ProfileDetailsDto;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/user-service/user")
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserAuthService userAuthService;
    private final ProfileService profileService;
    private final CommonService commonService;
    private final UserCommonService userCommonService;

    @Operation(summary = "Api end point to test")
    @GetMapping("/test")
    public String testFunction(){
        return "method entered";
    }

    @Operation(summary = "Api to change password for logged in user")
    @PostMapping("/change-password")
    public void changePassword(Authentication authentication,
                               @Valid @RequestBody ChangePasswordDto changePasswordDto) {
        Long userId = userAuthService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        changePasswordDto.setUserId(userId);
        userAuthService.changePassword(changePasswordDto);
    }

    @Operation(summary = "Api to send otp to block/delete the account")
    @GetMapping("/otp-request/account-deactivate-actions")
    public ResponseEntity<String> sendOtpForBlockAndDeleteAccountByUserRequest(Authentication authentication,
                                                                               @NotBlank @RequestParam String type){
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userAuthService.sendOtpToBlockAccount(username, type);
    }

    @Operation(summary = "Api to save/update the profile details of a user")
    @PostMapping("/profile-details/save")
    public ResponseEntity<ProfileDetailsDto> saveProfileDetails(Authentication authentication,
                                                                @RequestBody ProfileModel profile){
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return ResponseEntity.ok(profileService.saveUserDetails(username, userAuthService.getUserIdByUsername(username), profile));
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
        Long userId = userAuthService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        return ResponseEntity.ok(profileService.getUserDetailsByUserId(userId));
    }

    @Operation(summary = "Api to save the user raised defect details")
    @PostMapping("/report-issue")
    public void saveUserRaisedReports(Authentication authentication,
                                      @Valid @ModelAttribute UserDefectRequestDto userDefectRequestDto) throws IOException {
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        profileService.saveContactUsDetails(userDefectRequestDto, userAuthService.getUserIdByUsername(username), username);
    }

    @Operation(summary = "Api to save the user feedback details")
    @PostMapping("/submit-feedback")
    public void saveUserFeedback(Authentication authentication,
                                 @RequestBody @Valid UserFeedbackRequestDto feedback){
        Long userId = userAuthService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        profileService.saveFeedback(feedback, userId);
    }

    @Operation(summary = "Api to send user's account statement as email")
    @PostMapping("/account-statement/email")
    public ResponseEntity<Void> sendAccountStatementEmailToUser(Authentication authentication,
                                                                @RequestBody byte[] pdfBytes) {
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        if(!commonService.sendAccountStatementEmail(username, userAuthService.getUserIdByUsername(username), pdfBytes)){
            throw new ResourceNotFoundException("Error in sending email, internal error");
        }
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Api to send user's account spending analysis as email")
    @PostMapping("/spending-analysis/email")
    public ResponseEntity<Void> sendSpendingAnalysisEmailToUser(Authentication authentication,
                                                                @RequestBody byte[] pdfBytes) {
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        if(!commonService.sendSpendingAnalysisEmail(username, userAuthService.getUserIdByUsername(username), pdfBytes)){
            throw new ResourceNotFoundException("Error in sending email, internal error");
        }
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Api to upload profile pic to AWS S3")
    @PostMapping("/profile-picture/upload")
    public ResponseEntity<String> uploadUserProfilePictureToS3(Authentication authentication,
                                                               @RequestParam(value = "file") MultipartFile file) throws IOException {
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return ResponseEntity.ok(userCommonService.uploadUserProfilePictureToS3(username, userAuthService.getUserIdByUsername(username), file));
    }

    @Operation(summary = "Api to fetch the user profile picture from aws s3")
    @GetMapping("/profile-picture/get")
    public ResponseEntity<ByteArrayResource> fetchUserProfilePictureFromS3(Authentication authentication) {
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userCommonService.fetchUserProfilePictureFromS3(username, userAuthService.getUserIdByUsername(username));
    }

    @Operation(summary = "Api to delete the user profile picture from aws s3")
    @DeleteMapping("/profile-picture/delete")
    public ResponseEntity<String> deleteProfilePictureFromS3(Authentication authentication) {
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userCommonService.deleteProfilePictureFromS3(username, userAuthService.getUserIdByUsername(username));
    }

    @Operation(summary = "Api to get the admin scheduled notifications")
    @GetMapping("/notifications/get")
    public ResponseEntity<List<UserNotificationResponseDto>> getUserNotifications(Authentication authentication,
                                                                                  @NotBlank @RequestParam(value = "status") String status){
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return ResponseEntity.ok(userCommonService.getUserNotifications(username, status));
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
                                                 @NotBlank @RequestParam(value = "ids") String notificationIds){
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        userCommonService.updateUserNotificationSeenStatus(username, notificationIds);
    }

    @Operation(summary = "Api to download the empty template of excel for user profile details to be saved")
    @GetMapping("/excel-template/download")
    public ResponseEntity<byte[]> downloadTemplateForUserProfile(@NotBlank @RequestParam(value = "type") String fileType) {
        return profileService.downloadTemplateForUserProfile(fileType);
    }

    @Operation(summary = "Api to parse the excel to extract user's profile data and save into db")
    @PostMapping("/user-profile/excel-upload")
    public void parseUserProfileDataFromExcel(Authentication authentication,
                                              @RequestParam(value = "file") MultipartFile excel){
        Long userId = userAuthService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        profileService.parseUserProfileDataFromExcel(excel, userId);
    }

    @Operation(summary = "Api to block/delete account based on user request")
    @PostMapping("/deactivate-account")
    public ResponseEntity<String> blockOrDeleteAccountByUserRequest(Authentication authentication,
                                                                    @RequestBody @Valid AccountBlockOrDeleteRequestDto request) {
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userCommonService.blockOrDeleteAccountByUserRequest(username, request);
    }

    @Operation(summary = "Api to request Admin to increase the gmail sync count")
    @PostMapping(value = "/user-request/gmail-sync-request", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void userRequestToIncreaseGmailSyncDailyCount(Authentication authentication,
                                                         @RequestPart(value = "data") @Valid GmailSyncCountIncreaseRequestDto request,
                                                         @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        userCommonService.userRequestToIncreaseGmailSyncDailyCount(request, image, username);
    }
}
