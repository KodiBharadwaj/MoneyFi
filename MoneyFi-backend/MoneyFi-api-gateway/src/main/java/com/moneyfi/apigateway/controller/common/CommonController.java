package com.moneyfi.apigateway.controller.common;

import com.moneyfi.apigateway.service.userservice.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/common")
public class CommonController {

    private final UserService userService;

    CommonController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Api to logout/making the token blacklist")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logoutUser(@RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return ResponseEntity.ok(userService.logout(token));
    }

    @Operation(summary = "Api to increase otp expiration time on user request")
    @GetMapping("/extend-session")
    public ResponseEntity<String> updateUserSessionExpirationTime(Authentication authentication,
                                                                  @RequestHeader("Authorization") String authHeader,
                                                                  @RequestParam long minutes) {
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return ResponseEntity.ok(userService.updateUserSessionExpirationTime(minutes, username, authHeader.substring(7)));
    }
}