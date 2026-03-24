package com.moneyfi.user.controller.common;

import com.moneyfi.user.service.user.UserAuthService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/user-service/common")
@RequiredArgsConstructor
public class CommonController {

    private final UserAuthService userAuthService;

    @Operation(summary = "Api to logout/making the token blacklist")
    @PostMapping("/logout")
    @PreAuthorize("hasAnyRole('USER','ADMIN','MAINTAINER')")
    public ResponseEntity<Map<String, String>> logoutUser(@RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return ResponseEntity.ok(userAuthService.logout(token));
    }

    @Operation(summary = "Api to increase otp expiration time on user request")
    @GetMapping("/extend-session")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<String> updateUserSessionExpirationTime(Authentication authentication,
                                                                  @RequestHeader("Authorization") String authHeader,
                                                                  @RequestParam long minutes) {
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return ResponseEntity.ok(userAuthService.updateUserSessionExpirationTime(minutes, username, authHeader.substring(7)));
    }

    @Operation(summary = "Api to get the username from token and simply return")
    @GetMapping("/get-username")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<String> getUsername(Authentication authentication){
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return ResponseEntity.ok(username);
    }
}
