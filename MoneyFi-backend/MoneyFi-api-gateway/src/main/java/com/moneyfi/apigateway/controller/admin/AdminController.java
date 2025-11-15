package com.moneyfi.apigateway.controller.admin;

import com.moneyfi.apigateway.exceptions.ResourceNotFoundException;
import com.moneyfi.apigateway.service.admin.AdminService;
import com.moneyfi.apigateway.service.admin.dto.request.*;
import com.moneyfi.apigateway.service.admin.dto.response.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService){
        this.adminService = adminService;
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
    public ResponseEntity<String> scheduleNotification(@RequestBody @Valid ScheduleNotificationRequestDto requestDto, HttpServletRequest request){
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }
        String token = authHeader.substring(7);
        return ResponseEntity.ok(adminService.scheduleNotification(requestDto, token));
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
}
