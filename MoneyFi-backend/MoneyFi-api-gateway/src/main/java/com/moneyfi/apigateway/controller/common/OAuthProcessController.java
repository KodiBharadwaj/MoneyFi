package com.moneyfi.apigateway.controller.common;

import com.moneyfi.apigateway.service.userservice.UserService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/Oauth")
public class OAuthProcessController {

    private final UserService userService;
    public OAuthProcessController(UserService userService){
        this.userService = userService;
    }

    @PostMapping("/google/callback")
    public ResponseEntity<Map<String, String>> handleGoogleCallback(@RequestBody Map<String, String> googleAuthToken) {
        return userService.loginViaGoogleOAuth(googleAuthToken);
    }

    @GetMapping("/github/popup-callback")
    public String handleGithubPopup(@RequestParam("code") String code) {
        return userService.loginViaGithubOAuth(code);
    }
}
