package com.moneyfi.transaction.controller;

import com.moneyfi.transaction.service.admin.AdminService;
import com.moneyfi.transaction.service.admin.dto.response.TransactionCategoryResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transaction/income/admin")
public class AdminApiController {

    private final AdminService adminService;

    public AdminApiController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/transaction-categories")
    public ResponseEntity<TransactionCategoryResponse> getCategoryWiseTransactionSummary() {
        return ResponseEntity.ok(adminService.getCategoryWiseTransactionSummary());
    }
}
