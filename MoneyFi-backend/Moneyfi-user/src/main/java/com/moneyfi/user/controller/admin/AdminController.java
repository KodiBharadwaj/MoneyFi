package com.moneyfi.user.controller.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.moneyfi.user.config.JwtService;
import com.moneyfi.user.service.admin.AdminService;
import com.moneyfi.user.service.admin.dto.request.*;
import com.moneyfi.user.service.admin.dto.response.*;
import com.moneyfi.user.service.common.UserCommonService;
import com.moneyfi.user.service.common.dto.response.UserFeedbackResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user-service/admin")
public class AdminController {

    private final AdminService adminService;
    private final UserCommonService userCommonService;
    private final JwtService jwtService;

    public AdminController(AdminService adminService,
                           UserCommonService userCommonService,
                           JwtService jwtService){
        this.adminService = adminService;
        this.userCommonService = userCommonService;
        this.jwtService = jwtService;
    }

    @Operation(summary = "Api to get the user count home page of admin")
    @GetMapping("/overview-user-details")
    public ResponseEntity<AdminOverviewPageDto> getAdminOverviewPageDetails(){
        return ResponseEntity.ok(adminService.getAdminOverviewPageDetails());
    }

    @Operation(summary = "Api to get active user grid details")
    @GetMapping("/user-details/grid")
    public List<UserGridDto> getUserDetailsGridForAdmin(@RequestParam("status") String status){
        return adminService.getUserDetailsGridForAdmin(status);
    }

    @Operation(summary = "Api to get active user defects raised details")
    @GetMapping("/user-defects/grid")
    public List<UserDefectResponseDto> getUserRaisedDefectsForAdmin(@RequestParam("status") String status){
        return adminService.getUserRaisedDefectsForAdmin(status);
    }

    @Operation(summary = "Api to get defect/user raised report image")
    @GetMapping("/user-defects/image")
    public ResponseEntity<ByteArrayResource> fetchUserRaisedDefectImage(@RequestParam String username,
                                                                        @RequestParam String type,
                                                                        @RequestParam Long id) {
        return userCommonService.getUserRaisedDefectImage(username, type, id);
    }

    @Operation(summary = "Api to change the user defect status in contact us table")
    @PutMapping("/{defectId}/update-defect-status")
    public void updateDefectStatus(@RequestHeader("Authorization") String authHeader,
                                   @PathVariable("defectId") Long defectId,
                                   @RequestBody Map<String, String> body,
                                   @RequestParam String reason) {
        Long adminUserId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        adminService.updateDefectStatus(defectId, body.get("status"), reason, adminUserId);
    }

    @Operation(summary = "Api to unblock/retrieve/name change of the user account with respective details")
    @PostMapping("/user-requests/action")
    public boolean accountReactivationAndNameChangeRequest(@RequestHeader("Authorization") String authHeader,
                                                           @RequestBody UserRequestsApprovalDto requestDto){
        Long adminUserId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return adminService.accountReactivationAndNameChangeRequest(requestDto.getEmail(), requestDto.getReferenceNumber(), requestDto.getRequestStatus(), adminUserId, requestDto.getApproveStatus(), requestDto.getDeclineReason(), requestDto.getGmailSyncRequestCount());
    }

    @Operation(summary = "Api to block the user's account by admin")
    @PostMapping("/user-account/block")
    public ResponseEntity<String> blockTheUserAccountByAdmin(@RequestHeader("Authorization") String authHeader,
                                                             @RequestParam String email,
                                                             @RequestParam String reason,
                                                             @RequestParam MultipartFile file) throws JsonProcessingException {
        Long adminUserId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return ResponseEntity.ok(adminService.blockTheUserAccountByAdmin(email, reason, file, adminUserId));
    }

    @Operation(summary = "Api to get user grid details as excel report")
    @GetMapping("/user-details/excel")
    public ResponseEntity<byte[]> getUserDetailsExcelForAdmin(@RequestParam("status") String status){
        byte[] excelData = adminService.getUserDetailsExcelForAdmin(status);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+status+"_user_list.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelData);
    }

    @Operation(summary = "Api to get the user requests for admin")
    @GetMapping("/fetch-user-requests/grid")
    public List<UserRequestsGridDto> getUserRequestsGridForAdmin(@RequestParam("status") String status){
        return adminService.getUserRequestsGridForAdmin(status);
    }

    @Operation(summary = "Api to get the user feedback list to the admin")
    @GetMapping("/user-feedback/get")
    public List<UserFeedbackResponseDto> getUserFeedbackListForAdmin(){
        return adminService.getUserFeedbackListForAdmin();
    }

    @Operation(summary = "Api to update the user feedback by admin")
    @PutMapping("/user-feedback/update")
    public void updateUserFeedback(@RequestHeader("Authorization") String authHeader,
                                   @RequestParam("id") Long feedbackId){
        Long adminUserId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        adminService.updateUserFeedback(feedbackId, adminUserId);
    }

    @Operation(summary = "Api to the user count in every month for chart")
    @GetMapping("/{year}/user-monthly-count/chart")
    public Map<Integer, Integer> getUserMonthlyCountInAYear(@PathVariable("year") int year,
                                                            @RequestParam("status") String status){
        return adminService.getUserMonthlyCountInAYear(year, status);
    }

    @Operation(summary = "Api to get the user profile details for admin")
    @GetMapping("/user-profile-details")
    public UserProfileAndRequestDetailsDto getCompleteUserDetailsForAdmin(@RequestParam("username") String username){
        try {
            return adminService.getCompleteUserDetailsForAdmin(username);
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Operation(summary = "Api to get the user defect history details for admin")
    @PostMapping("/user-defects/history-details")
    public ResponseEntity<Map<String, List<UserDefectHistDetailsResponseDto>>> getUserDefectHistDetails(@RequestBody List<Long> defectIds){
        Map<String, List<UserDefectHistDetailsResponseDto>> response = adminService.getUserDefectHistDetails(defectIds);
        if(!response.isEmpty()){
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @Operation(summary = "Api to add the reason dropdown names")
    @PostMapping("/reasons/add")
    public void addReasonsForUserReasonDialog(@RequestHeader("Authorization") String authHeader,
                                              @RequestBody ReasonDetailsRequestDto requestDto){
        Long adminUserId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        adminService.addReasonsForUserReasonDialog(requestDto, adminUserId);
    }

    @Operation(summary = "Api to get the reason based on reason code")
    @GetMapping("/reasons/get")
    public ResponseEntity<List<ReasonListResponseDto>> getAllReasonsBasedOnReasonCode(@RequestParam("code") int reasonCode){
        List<ReasonListResponseDto> responseList = adminService.getAllReasonsBasedOnReasonCode(reasonCode);
        if(!responseList.isEmpty()){
            return ResponseEntity.status(HttpStatus.OK).body(responseList);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @Operation(summary = "Api to update the reason string")
    @PutMapping("/reasons/update")
    public void updateReasonsForUserReasonDialogByReasonCode(@RequestHeader("Authorization") String authHeader,
                                                             @RequestBody ReasonUpdateRequestDto requestDto){
        Long adminUserId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        adminService.updateReasonsForUserReasonDialogByReasonCode(requestDto, adminUserId);
    }

    @Operation(summary = "Api to delete the reason names by admin - soft delete")
    @DeleteMapping("/reasons/delete")
    public void deleteReasonByReasonIdByAdmin(@RequestHeader("Authorization") String authHeader,
                                              @RequestParam("id") int reasonId){
        Long adminUserId = jwtService.extractUserIdFromToken(authHeader.substring(7));
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
    public void scheduleNotification(@RequestHeader("Authorization") String authHeader,
                                     @RequestBody @Valid ScheduleNotificationRequestDto requestDto){
        Long adminUserId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        adminService.scheduleNotification(requestDto, adminUserId);
    }

    @Operation(summary = "Api to get all the schedules for admin screen")
    @GetMapping("/schedule-notifications/get")
    public ResponseEntity<List<AdminSchedulesResponseDto>> getAllActiveSchedulesOfAdmin(@RequestParam("status") String status){
        return ResponseEntity.ok(adminService.getAllActiveSchedulesOfAdmin(status));
    }

    @Operation(summary = "Api to cancel the user scheduling")
    @PutMapping("/schedule-notification/cancel")
    public void cancelTheUserScheduling(@RequestHeader("Authorization") String authHeader,
                                        @RequestParam("id") Long scheduleId){
        Long adminUserId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        adminService.cancelTheUserScheduling(scheduleId, adminUserId);
    }

    @Operation(summary = "Api to soft delete the user scheduling")
    @DeleteMapping("/schedule-notification/delete")
    public void deleteUserScheduling(@RequestHeader("Authorization") String authHeader,
                                     @RequestParam("id") Long scheduleId){
        Long adminUserId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        adminService.deleteUserScheduling(scheduleId, adminUserId);
    }

    @Operation(summary = "Api to update the already scheduled notification")
    @PutMapping("/schedule-notification/update")
    public void updateAdminPlacedSchedules(@RequestHeader("Authorization") String authHeader,
                                           @RequestBody @Valid AdminScheduleRequestDto requestDto){
        Long adminUserId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        adminService.updateAdminPlacedSchedules(requestDto, adminUserId);
    }
}
