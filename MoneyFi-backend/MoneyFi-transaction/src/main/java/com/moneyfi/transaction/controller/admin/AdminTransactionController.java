package com.moneyfi.transaction.controller.admin;

import com.moneyfi.constants.enums.TransactionServiceType;
import com.moneyfi.transaction.batch.service.TriggerBatchJob;
import com.moneyfi.transaction.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transaction/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AdminTransactionController {

    private final TriggerBatchJob triggerBatchJob;
    private final JwtService jwtService;

    @Operation(summary = "Api to trigger batch")
    @GetMapping(value = "batch-sync")
    public void enableRecurringSyncUsingSpringBatch(@NotBlank @RequestHeader("Authorization") String authHeader,
                                                    @NotNull TransactionServiceType type,
                                                    @RequestParam(required = false) String username) {
        log.info("checking username: {}", username);
        Long adminUserId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        triggerBatchJob.triggerBatchJob(type, adminUserId, username);
    }
}
