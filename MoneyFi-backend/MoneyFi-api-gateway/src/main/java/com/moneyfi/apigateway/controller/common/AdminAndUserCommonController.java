package com.moneyfi.apigateway.controller.common;

import com.moneyfi.apigateway.service.common.UserCommonService;
import com.moneyfi.apigateway.service.userservice.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user-admin")
public class AdminAndUserCommonController {

    private final UserCommonService userCommonService;
    private final UserService userService;

    AdminAndUserCommonController(UserCommonService userCommonService,
                                 UserService userService) {
        this.userCommonService = userCommonService;
        this.userService = userService;
    }

    @Operation(summary = "Api to get reasons for respected reason codes")
    @GetMapping("/reasons-dialog/get")
    public ResponseEntity<List<String>> getReasonsForDialogForUser(@RequestParam("code") int reasonCode){
        List<String> responseList = userCommonService.getReasonsForDialogForUser(reasonCode);
        return !responseList.isEmpty() ? ResponseEntity.status(HttpStatus.OK).body(responseList) :
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @Operation(summary = "Api to logout/making the token blacklist")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logoutUser(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(userService.logout(token));
    }
}