package com.moneyfi.user.controller.user;

import com.moneyfi.user.service.user.UserAuthService;
import com.moneyfi.user.service.gmailsync.GmailSyncService;
import com.moneyfi.user.service.gmailsync.dto.response.GmailConsentResponse;
import com.moneyfi.user.service.gmailsync.dto.response.GmailSyncHistoryResponse;
import com.moneyfi.user.service.gmailsync.dto.response.ParsedTransaction;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user-service/gmail-sync")
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
public class GmailSyncController {

    private final GmailSyncService gmailSyncService;
    private final UserAuthService userAuthService;

    @Operation(summary = "Api to silent verification without further google consent")
    @PostMapping("/enable")
    public void enableSync(Authentication authentication,
                           @RequestBody Map<String, String> body) {
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        gmailSyncService.enableSync(body.get("code"), username, userAuthService.getUserIdByUsername(username));
    }

    @Operation(summary = "Api to check the status of user to sync email")
    @GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GmailConsentResponse<Integer>> getStatus(Authentication authentication) {
        Long userId = userAuthService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        return ResponseEntity.ok(gmailSyncService.getGmailConsentStatus(userId));
    }

    @Operation(summary = "Api to sync transaction related emails and return data to user to verify")
    @PostMapping("/start")
    public ResponseEntity<Map<Integer, List<ParsedTransaction>>> startGmailSync(Authentication authentication,
                                                                                @RequestParam LocalDate date) throws IOException, URISyntaxException {
        Long userId = userAuthService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        return ResponseEntity.ok(gmailSyncService.startGmailSync(userId, date));
    }

    @Operation(summary = "Api to get history gmail sync data")
    @GetMapping("/history")
    public ResponseEntity<List<GmailSyncHistoryResponse>> getSyncHistoryResponse(Authentication authentication) {
        Long userId = userAuthService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        return ResponseEntity.ok(gmailSyncService.getSyncHistoryResponse(userId));
    }
}
