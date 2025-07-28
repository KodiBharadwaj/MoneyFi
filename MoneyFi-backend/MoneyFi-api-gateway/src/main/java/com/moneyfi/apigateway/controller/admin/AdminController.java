package com.moneyfi.apigateway.controller.admin;

import com.moneyfi.apigateway.service.admin.AdminService;
import com.moneyfi.apigateway.service.admin.dto.response.*;
import com.moneyfi.apigateway.service.userservice.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpHeaders;
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
    @GetMapping("user-profile-details")
    public UserProfileAndRequestDetailsDto getCompleteUserDetailsForAdmin(@RequestParam("username") String username){
        return adminService.getCompleteUserDetailsForAdmin(username);
    }

    @Operation(summary = "Api to logout/making the token blacklist for admin")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logoutUser(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(userService.logout(token));
    }
}
