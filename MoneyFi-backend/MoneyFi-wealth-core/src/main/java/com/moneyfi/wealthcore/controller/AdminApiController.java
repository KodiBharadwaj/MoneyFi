package com.moneyfi.wealthcore.controller;

import com.moneyfi.wealthcore.config.JwtService;
import com.moneyfi.wealthcore.service.admin.AdminService;
import com.moneyfi.wealthcore.service.admin.dto.request.CategoryRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wealth-core/admin")
public class AdminApiController {

    private final AdminService adminService;
    private final JwtService jwtService;

    public AdminApiController(AdminService adminService,
                              JwtService jwtService){
        this.adminService = adminService;
        this.jwtService = jwtService;
    }

    @Operation(summary = "Api to fetch the category type from enum")
    @GetMapping("/category-type/get")
    public ResponseEntity<List<String>> getCategoryType() {
        return ResponseEntity.ok(adminService.getCategoryType());
    }

    @Operation(summary = "Api to save the category list")
    @PostMapping("/category-list/save")
    public void saveCategoryWiseList(@RequestHeader("Authorization") String authHeader,
                                     @Valid @RequestBody CategoryRequestDto requestDto) {
        Long adminUserId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        adminService.saveCategoryWiseList(adminUserId, requestDto);
    }

    @Operation(summary = "Api to update the category list")
    @PutMapping("/category-list/{categoryId}/update")
    public void updateCategoryWiseList(@RequestHeader("Authorization") String authHeader,
                                       @PathVariable("categoryId") Integer categoryId,
                                       @Valid @RequestBody CategoryRequestDto requestDto) {
        Long adminUserId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        adminService.updateCategoryWiseList(adminUserId, categoryId, requestDto);
    }

    @Operation(summary = "Api to delete the category list")
    @DeleteMapping("/category-list/{categoryId}/delete")
    public void deleteCategoryWiseList(@RequestHeader("Authorization") String authHeader,
                                       @PathVariable("categoryId") Integer categoryId) {
        Long adminUserId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        adminService.deleteCategoryWiseList(adminUserId, categoryId);
    }
}
