package com.moneyfi.apigateway.controller;

import com.moneyfi.apigateway.model.ProfileModel;
import com.moneyfi.apigateway.service.profileservice.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/userProfile")
public class ProfileApiController {

    private final ProfileService profileService;

    public ProfileApiController(ProfileService profileService){
        this.profileService = profileService;
    }

    @Operation(summary = "Method to save the profile details of a user")
    @PostMapping("/{userId}")
    public ProfileModel saveProfile(@PathVariable("userId") Long userId,
                                    @RequestBody ProfileModel profile){

        return profileService.saveUserDetails(userId, profile);
    }

    @Operation(summary = "Method to get the profile details of a user")
    @GetMapping("/{userId}")
    public ProfileModel getProfile(@PathVariable("userId") Long userId){
        return profileService.getUserDetailsByUserId(userId);
    }

    @Operation(summary = "Method to get the name of a user")
    @GetMapping("/getName/{userId}")
    public String getNameFromUserId(@PathVariable("userId") Long userId){
        return profileService.getNameByUserId(userId);
    }
}
