package com.moneyfi.apigateway.controller.gmailsync;

import com.moneyfi.apigateway.dto.ParsedTransaction;
import com.moneyfi.apigateway.service.gmailsync.GmailSyncService;
import com.moneyfi.apigateway.service.userservice.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/gmail-sync")
@RequiredArgsConstructor
public class GmailSyncController {

    private final GmailSyncService gmailSyncService;
    private final UserService userService;

    @Operation(summary = "Api to sync transaction related emails and return data to user to verify")
    @PostMapping("/enable")
    public ResponseEntity<List<ParsedTransaction>> enableSync(Authentication authentication,
                                                              @RequestBody Map<String, String> body) throws IOException {
        Long userId = userService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        return ResponseEntity.ok(gmailSyncService.enableSync(body.get("code"), userId));
    }

    @Operation(summary = "Api to check the status of user to sync email")
    @GetMapping("/status")
    public ResponseEntity<Boolean> getStatus(Authentication authentication) {
        Long userId = userService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        return ResponseEntity.ok(gmailSyncService.isSyncEnabled(userId));
    }
}
