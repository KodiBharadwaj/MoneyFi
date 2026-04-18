package com.moneyfi.user.controller.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.moneyfi.user.service.admin.AdminService;
import com.moneyfi.user.service.admin.dto.request.*;
import com.moneyfi.user.service.admin.dto.response.*;
import com.moneyfi.user.service.general.rabbitmq.RabbitMqQueuePublisher;
import com.moneyfi.user.service.user.UserAuthService;
import com.moneyfi.user.service.user.UserCommonService;
import com.moneyfi.user.service.user.dto.response.UserFeedbackResponseDto;
import com.moneyfi.user.service.user.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user-service/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Validated
public class AdminController {

    private final AdminService adminService;
    private final UserCommonService userCommonService;
    private final ProfileService profileService;
    private final UserAuthService userAuthService;

    @Operation(summary = "Api to get the user count home page of admin")
    @GetMapping("/overview-user-details")
    public ResponseEntity<AdminOverviewPageDto> getAdminOverviewPageDetails(){
        return ResponseEntity.ok(adminService.getAdminOverviewPageDetails());
    }

    @Operation(summary = "Api to get active user grid details")
    @GetMapping("/user-details/grid")
    public List<UserGridDto> getUserDetailsGridForAdmin(@NotBlank @RequestParam(value = "status") String status){
        return adminService.getUserDetailsGridForAdmin(status);
    }

    @Operation(summary = "Api to get active user defects raised details")
    @GetMapping("/user-defects/grid")
    public List<UserDefectResponseDto> getUserRaisedDefectsForAdmin(@NotBlank @RequestParam(value = "status") String status){
        return adminService.getUserRaisedDefectsForAdmin(status);
    }

    @Operation(summary = "Api to get defect/user raised report image")
    @GetMapping("/user-defects/image")
    public ResponseEntity<ByteArrayResource> fetchUserRaisedDefectImage(@NotBlank @RequestParam String username,
                                                                        @NotBlank @RequestParam String type,
                                                                        @NotNull @RequestParam Long id) {
        return userCommonService.getUserRaisedDefectImage(username, type, id);
    }

    @Operation(summary = "Api to change the user defect status in contact us table")
    @PutMapping("/{defectId}/update-defect-status")
    public void updateDefectStatus(Authentication authentication,
                                   @NotNull @PathVariable(value = "defectId") Long defectId,
                                   @RequestBody Map<String, String> body,
                                   @NotBlank @RequestParam String reason) {
        Long adminUserId = userAuthService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        adminService.updateDefectStatus(defectId, body.get("status"), reason, adminUserId);
    }

    @Operation(summary = "Api to unblock/retrieve/name change of the user account with respective details")
    @PostMapping("/user-requests/action")
    public boolean accountReactivationAndNameChangeRequest(Authentication authentication,
                                                           @RequestBody @Valid UserRequestsApprovalDto requestDto){
        Long adminUserId = userAuthService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        return adminService.accountReactivationAndNameChangeRequest(requestDto.getEmail(), requestDto.getReferenceNumber(), requestDto.getRequestStatus(), adminUserId, requestDto.getApproveStatus(), requestDto.getDeclineReason(), requestDto.getGmailSyncRequestCount());
    }

    @Operation(summary = "Api to block the user's account by admin")
    @PostMapping(value = "/user-account/block", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void blockTheUserAccountByAdmin(Authentication authentication,
                                                             @NotBlank @RequestParam String email,
                                                             @NotBlank @RequestParam String reason,
                                                             @RequestParam(required = true) MultipartFile file) throws JsonProcessingException {
        Long adminUserId = userAuthService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        adminService.blockTheUserAccountByAdmin(email, reason, file, adminUserId);
    }

    @Operation(summary = "Api to get user grid details as excel report")
    @GetMapping("/user-details/excel")
    public ResponseEntity<byte[]> getUserDetailsExcelForAdmin(@NotBlank @RequestParam(value = "status") String status){
        byte[] excelData = adminService.getUserDetailsExcelForAdmin(status);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+status+"_user_list.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelData);
    }

    @Operation(summary = "Api to get the user requests for admin")
    @GetMapping("/fetch-user-requests/grid")
    public List<UserRequestsGridDto> getUserRequestsGridForAdmin(@NotBlank @RequestParam(value = "status") String status){
        return adminService.getUserRequestsGridForAdmin(status);
    }

    @Operation(summary = "Api to get the user feedback list to the admin")
    @GetMapping("/user-feedback/get")
    public List<UserFeedbackResponseDto> getUserFeedbackListForAdmin(){
        return adminService.getUserFeedbackListForAdmin();
    }

    @Operation(summary = "Api to update the user feedback by admin")
    @PutMapping("/user-feedback/update")
    public void updateUserFeedback(Authentication authentication,
                                   @NotNull @RequestParam(value = "id") Long feedbackId){
        Long adminUserId = userAuthService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        adminService.updateUserFeedback(feedbackId, adminUserId);
    }

    @Operation(summary = "Api to the user count in every month for chart")
    @GetMapping("/{year}/user-monthly-count/chart")
    public Map<Integer, Integer> getUserMonthlyCountInAYear(@NotNull @PathVariable(value = "year") int year,
                                                            @NotBlank @RequestParam(value = "status") String status){
        return adminService.getUserMonthlyCountInAYear(year, status);
    }

    @Operation(summary = "Api to get the user profile details for admin")
    @GetMapping("/user-profile-details")
    public ResponseEntity<UserProfileAndRequestDetailsDto> getCompleteUserDetailsForAdmin(Authentication authentication,
                                                                                          @NotBlank @RequestParam(value = "username") String username){
        Long adminUserId = userAuthService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        return ResponseEntity.ok(adminService.getCompleteUserDetailsForAdmin(username, adminUserId));
    }

    @Operation(summary = "Api to fetch the user profile picture from aws s3")
    @GetMapping("/profile-picture/get")
    public ResponseEntity<ByteArrayResource> fetchUserProfilePictureFromS3(@NotBlank @RequestParam(value = "username") String username) {
        return userCommonService.fetchUserProfilePictureFromS3(username, profileService.getProfileDetailsOfUser(username).getUserId());
    }

    @Operation(summary = "Api to add the reason dropdown names")
    @PostMapping("/reasons/add")
    public void addReasonsForUserReasonDialog(Authentication authentication,
                                              @RequestBody @Valid ReasonDetailsRequestDto requestDto){
        Long adminUserId = userAuthService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        adminService.addReasonsForUserReasonDialog(requestDto, adminUserId);
    }

    @Operation(summary = "Api to get the reason based on reason code")
    @GetMapping("/reasons/get")
    public ResponseEntity<List<ReasonListResponseDto>> getAllReasonsBasedOnReasonCode(@NotNull @RequestParam(value = "code") int reasonCode){
        List<ReasonListResponseDto> responseList = adminService.getAllReasonsBasedOnReasonCode(reasonCode);
        if(!responseList.isEmpty()){
            return ResponseEntity.status(HttpStatus.OK).body(responseList);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @Operation(summary = "Api to update the reason string")
    @PutMapping("/reasons/update")
    public void updateReasonsForUserReasonDialogByReasonCode(Authentication authentication,
                                                             @RequestBody @Valid ReasonUpdateRequestDto requestDto){
        Long adminUserId = userAuthService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        adminService.updateReasonsForUserReasonDialogByReasonCode(requestDto, adminUserId);
    }

    @Operation(summary = "Api to delete the reason names by admin - soft delete")
    @DeleteMapping("/reasons/delete")
    public void deleteReasonByReasonIdByAdmin(Authentication authentication,
                                              @NotNull @RequestParam(value = "id") int reasonId){
        Long adminUserId = userAuthService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        adminService.deleteReasonByReasonId(reasonId, adminUserId);
    }

    @Operation(summary = "Api to get all the usernames of all the users for the admin")
    @GetMapping("/get-usernames")
    public ResponseEntity<List<String>> getUsernamesOfAllUsers(){
        List<String> usernamesList = adminService.getUsernamesOfAllUsers();
        return ResponseEntity.ok(usernamesList);
    }

    @Operation(summary = "Api to schedule a notification by admin")
    @PostMapping("/schedule-notification")
    public void scheduleNotification(Authentication authentication,
                                     @RequestBody @Valid ScheduleNotificationRequestDto requestDto){
        Long adminUserId = userAuthService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        adminService.scheduleNotification(requestDto, adminUserId);
    }

    @Operation(summary = "Api to get all the schedules for admin screen")
    @GetMapping("/schedule-notifications/get")
    public ResponseEntity<List<AdminSchedulesResponseDto>> getAllActiveSchedulesOfAdmin(Authentication authentication,
                                                                                        @NotBlank @RequestParam(value = "status") String status,
                                                                                        @NotBlank @RequestParam(value = "mode") String operationMode){
        String adminUsername = ((UserDetails) authentication.getPrincipal()).getUsername();
        return ResponseEntity.ok(adminService.getAllActiveSchedulesOfAdmin(status, operationMode, adminUsername));
    }

    @Operation(summary = "Api to cancel the user scheduling")
    @PutMapping("/schedule-notification/cancel")
    public void cancelTheUserScheduling(Authentication authentication,
                                        @NotNull @RequestParam(value = "id") Long scheduleId){
        Long adminUserId = userAuthService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        adminService.cancelTheUserScheduling(scheduleId, adminUserId);
    }

    @Operation(summary = "Api to soft delete the user scheduling")
    @DeleteMapping("/schedule-notification/delete")
    public void deleteUserScheduling(Authentication authentication,
                                     @NotNull @RequestParam(value = "id") Long scheduleId){
        Long adminUserId = userAuthService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        adminService.deleteUserScheduling(scheduleId, adminUserId);
    }

    @Operation(summary = "Api to update the already scheduled notification")
    @PutMapping("/schedule-notification/update")
    public void updateAdminPlacedSchedules(Authentication authentication,
                                           @RequestBody @Valid AdminScheduleRequestDto requestDto){
        Long adminUserId = userAuthService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        adminService.updateAdminPlacedSchedules(requestDto, adminUserId);
    }

    @Operation(summary = "Api to upload excel templates into cloud and database(only when fallback occurs)")
    @PostMapping("/excel-template/upload")
    public void uploadTemplateExcel(Authentication authentication,
                                    @NotBlank @RequestParam(value = "type") String type,
                                    @NotBlank @RequestParam(value = "operation") String operation,
                                    @RequestParam(value = "file") MultipartFile file) throws IOException {
        Long adminUserId = userAuthService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        adminService.uploadExcelTemplate(adminUserId, type, operation, file);
    }

    @Operation(summary = "Api to get the list of excel templates")
    @GetMapping("/get-excel-templates")
    public ResponseEntity<List<ExcelTemplateList>> getExcelTemplates() {
        return ResponseEntity.ok(adminService.getAllExcelTemplates());
    }
}
