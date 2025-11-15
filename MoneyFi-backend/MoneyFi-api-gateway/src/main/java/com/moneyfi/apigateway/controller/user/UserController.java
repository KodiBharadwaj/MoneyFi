package com.moneyfi.apigateway.controller.user;

import com.moneyfi.apigateway.service.userservice.dto.request.ChangePasswordDto;
import com.moneyfi.apigateway.service.userservice.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/test")
    public Long testFunction(Authentication authentication){
        if(authentication.isAuthenticated()){
            String username = ((UserDetails) authentication.getPrincipal()).getUsername();
            return  userService.getUserIdByUsername(username);
        }
        return null;
    }

    @Operation(summary = "Api to get the user id from user's email")
    @GetMapping("/getUserId/{email}")
    public Long getUserIdByUserEmail(@PathVariable("email") String email){
        return userService.getUserIdByUsername(email);
    }

    @Operation(summary = "Api to get the username from token and simply return")
    @GetMapping("/get-username")
    public ResponseEntity<String> getUsername(Authentication authentication){
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return ResponseEntity.ok(username);
    }

    @Operation(summary = "Api to change password for logged in user")
    @PostMapping("/change-password")
    public void changePassword(Authentication authentication,
                                                @RequestBody ChangePasswordDto changePasswordDto) {
        Long userId = userService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        changePasswordDto.setUserId(userId);
        userService.changePassword(changePasswordDto);
    }

    @Operation(summary = "Api to send otp to block/delete the account")
    @GetMapping("/otp-request/account-deactivate-actions")
    public ResponseEntity<String> sendOtpForBlockAndDeleteAccountByUserRequest(Authentication authentication,
                                                                               @RequestParam String type){
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userService.sendOtpToBlockAccount(username, type);
    }
}
