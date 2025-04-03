package com.moneyfi.user.api;

import com.moneyfi.user.model.ProfileModel;
import com.moneyfi.user.service.profile.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileApiController {

    @Autowired
    private ProfileService profileService;

    @Operation(summary = "Method to save the profile details of a user")
    @PostMapping("/{userId}")
    public ProfileModel saveProfile(@PathVariable("userId") int userId, @RequestBody ProfileModel profile){
        return profileService.save(userId, profile);
    }

    @Operation(summary = "Method to get the profile details of a user")
    @GetMapping("/{userId}")
    public ProfileModel getProfile(@PathVariable("userId") int userId){
        return profileService.findByUserId(userId);
    }

    @Operation(summary = "Method to get the name of a user")
    @GetMapping("/getName/{userId}")
    public String getNameFromUserId(@PathVariable("userId") int userId){
        return profileService.getNameByUserId(userId);
    }
}
