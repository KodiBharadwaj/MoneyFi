package com.moneyfi.user.controller.admin;

import com.moneyfi.constants.dto.BatchInfoForEmailDto;
import com.moneyfi.user.service.general.inter.InterServiceCommunicationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user-service/admin-inter/")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Validated
public class InterServiceCommunicationController {

    private final InterServiceCommunicationService interServiceCommunicationService;

    @Operation(summary = "Api to send transaction update to user as email")
    @PostMapping("/batch-info/email")
    public ResponseEntity<Void> sendAccountStatementEmailToUser(@RequestBody List<BatchInfoForEmailDto> batchInfoList) {
        System.out.println("Api called here: ");
        interServiceCommunicationService.sendBatchInformationEmailToUser(batchInfoList);
        return ResponseEntity.ok().build();
    }
}
