package com.moneyfi.user.controller.internal;

import com.moneyfi.constants.dto.BatchInfoForEmailDto;
import com.moneyfi.user.service.general.inter.InterServiceCommunicationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user-service")
public class InterServiceCommunicationController {

    private final InterServiceCommunicationService interServiceCommunicationService;

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Api to send transaction update to user as email", description = "Send user's account generated secured statement as email")
    @PostMapping("/admin-inter/batch-info/email")
    public ResponseEntity<Void> sendAccountStatementEmailToUser(@RequestBody List<BatchInfoForEmailDto> batchInfoList) {
        interServiceCommunicationService.sendBatchInformationEmailToUser(batchInfoList);
        return ResponseEntity.ok().build();
    }
}
