package com.moneyfi.apigateway.controller.user;

import com.moneyfi.apigateway.service.userservice.GmailSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/gmail/sync")
@RequiredArgsConstructor
public class GmailSyncController {

    private final GmailSyncService gmailSyncService;

    @PostMapping("/enable")
    public ResponseEntity<Void> enableSync(@RequestBody Map<String, String> body) throws IOException {
        gmailSyncService.enableSync(body.get("code"), 1L);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status")
    public ResponseEntity<Boolean> getStatus() {
        return ResponseEntity.ok(gmailSyncService.isSyncEnabled(1L));
    }
}
