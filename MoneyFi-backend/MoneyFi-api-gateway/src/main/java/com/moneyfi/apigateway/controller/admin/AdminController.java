package com.moneyfi.apigateway.controller.admin;

import com.moneyfi.apigateway.model.common.ContactUs;
import com.moneyfi.apigateway.service.admin.AdminService;
import com.moneyfi.apigateway.service.admin.dto.AdminOverviewPageDto;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService){
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public String getAllUsers() {
        return "Admin role is working";
    }

    @Operation(summary = "Api to get the user count home page of admin")
    @GetMapping("/overview-user-details")
    public ResponseEntity<AdminOverviewPageDto> getAdminOverviewPageDetails(){
        return ResponseEntity.ok(adminService.getAdminOverviewPageDetails());
    }

    @Operation(summary = "Api to get the contact us details of the users")
    @GetMapping("/fetch-contact-us")
    public List<ContactUs> getContactUsDetailsOfUsers(){
        return adminService.getContactUsDetailsOfUsers();
    }
}
