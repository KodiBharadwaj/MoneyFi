package com.moneyfi.apigateway.controller.common;

import com.moneyfi.apigateway.service.userservice.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
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
        return ResponseEntity.ok(userService.logout(token));
    }
}