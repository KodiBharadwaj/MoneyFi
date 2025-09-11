package com.moneyfi.apigateway.controller.admin;

import com.moneyfi.apigateway.exceptions.ResourceNotFoundException;
import com.moneyfi.apigateway.service.admin.AdminService;
import com.moneyfi.apigateway.service.admin.dto.request.AdminScheduleRequestDto;
import com.moneyfi.apigateway.service.admin.dto.request.ReasonDetailsRequestDto;
import com.moneyfi.apigateway.service.admin.dto.request.ReasonUpdateRequestDto;
import com.moneyfi.apigateway.service.admin.dto.request.ScheduleNotificationRequestDto;
import com.moneyfi.apigateway.service.admin.dto.response.*;
import com.moneyfi.apigateway.service.common.dto.response.UserFeedbackResponseDto;
import com.moneyfi.apigateway.service.userservice.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;

    public AdminController(AdminService adminService,
                           UserService userService){
        this.adminService = adminService;
        this.userService = userService;
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

    @Operation(summary = "Api to change the user defect status in contact us table")
    @PutMapping("/{defectId}/update-defect-status")
    public void updateDefectStatus(@PathVariable("defectId") Long defectId,
                                                     @RequestBody Map<String, String> body) {
        adminService.updateDefectStatus(defectId, body.get("status"));
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
    public void updateUserFeedback(@RequestParam("id") Long feedbackId){
        adminService.updateUserFeedback(feedbackId);
    }

    @Operation(summary = "Api to unblock/retrieve/name change of the user account with respective details")
    @GetMapping("/admin-requests/{email}/{referenceNumber}/{requestStatus}")
    public boolean accountReactivationAndNameChangeRequest(@PathVariable("email") String email,
                                                           @PathVariable("referenceNumber") String referenceNumber,
                                                           @PathVariable("requestStatus") String requestStatus){
        return adminService.accountReactivationAndNameChangeRequest(email, referenceNumber, requestStatus);
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
        return adminService.getCompleteUserDetailsForAdmin(username);
    }

    @Operation(summary = "Api to get all the usernames of all the users for the admin")
    @GetMapping("/get-usernames")
    public ResponseEntity<List<String>> getUsernamesOfAllUsers(){
        List<String> usernamesList = adminService.getUsernamesOfAllUsers();
        if(usernamesList.isEmpty()){
            throw new ResourceNotFoundException("Users not found");
        }
        return ResponseEntity.ok(usernamesList);
    }

    @Operation(summary = "Api to schedule a notification by admin")
    @PostMapping("/schedule-notification")
    public ResponseEntity<String> scheduleNotification(@RequestBody @Valid ScheduleNotificationRequestDto requestDto){
        return ResponseEntity.ok(adminService.scheduleNotification(requestDto));
    }

    @Operation(summary = "Api to get all the schedules for admin screen")
    @GetMapping("/schedule-notifications/get")
    public ResponseEntity<List<AdminSchedulesResponseDto>> getAllActiveSchedulesOfAdmin(){
        List<AdminSchedulesResponseDto> scheduleList = adminService.getAllActiveSchedulesOfAdmin();
        if(!scheduleList.isEmpty()){
            return ResponseEntity.ok(scheduleList);
        } else {
            throw new ResourceNotFoundException("Data not found");
        }
    }

    @Operation(summary = "Api to cancel the user scheduling")
    @PutMapping("schedule-notification/cancel")
    public void cancelTheUserScheduling(@RequestParam("id") Long scheduleId){
        adminService.cancelTheUserScheduling(scheduleId);
    }

    @Operation(summary = "Api to update the already scheduled notification")
    @PutMapping("schedule-notification/update")
    public void updateAdminPlacedSchedules(@RequestBody @Valid AdminScheduleRequestDto requestDto){
        adminService.updateAdminPlacedSchedules(requestDto);
    }

    @Operation(summary = "Api to add the reason dropdown names")
    @PostMapping("/reasons/add")
    public void addReasonsForUserReasonDialog(@RequestBody ReasonDetailsRequestDto requestDto){
        adminService.addReasonsForUserReasonDialog(requestDto);
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
    public void updateReasonsForUserReasonDialogByReasonCode(@RequestBody ReasonUpdateRequestDto requestDto){
        adminService.updateReasonsForUserReasonDialogByReasonCode(requestDto);
    }

    @Operation(summary = "Api to delete the reason names by admin - soft delete")
    @DeleteMapping("/reasons/delete")
    public void deleteReasonByReasonIdByAdmin(@RequestParam("id") int reasonId){
        adminService.deleteReasonByReasonId(reasonId);
    }

    @Operation(summary = "Api to logout/making the token blacklist for admin")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logoutUser(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(userService.logout(token));
    }
}
