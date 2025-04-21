package com.moneyfi.apigateway.controller;

import com.moneyfi.apigateway.model.UserPrincipal;
import com.moneyfi.apigateway.model.common.ContactUs;
import com.moneyfi.apigateway.model.common.Feedback;
import com.moneyfi.apigateway.model.common.ProfileModel;
import com.moneyfi.apigateway.service.profileservice.ProfileService;
import com.moneyfi.apigateway.service.userservice.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/userProfile")
public class ProfileApiController {

    private final ProfileService profileService;
    private final UserService userService;

    public ProfileApiController(ProfileService profileService,
                                UserService userService){
        this.profileService = profileService;
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

    @Operation(summary = "Method to save the profile details of a user")
    @PostMapping("/{userId}")
    public ProfileModel saveProfile(@PathVariable("userId") Long userId,
                                    @RequestBody ProfileModel profile){

        return profileService.saveUserDetails(userId, profile);
    }

    @Operation(summary = "Method to get the profile details of a user")
    @GetMapping("/getProfile")
    public ResponseEntity<ProfileModel> getProfile(Authentication authentication){

        UserPrincipal user = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println("username check " + user.getUsername());

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            String username = userDetails.getUsername();
            Long userId = userService.getUserIdByUsername(username);
            ProfileModel profile = profileService.getUserDetailsByUserId(userId);

            if (profile != null) {
                return ResponseEntity.ok(profile);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @Operation(summary = "Method to get the name of a user")
    @GetMapping("/getName")
    public ResponseEntity<String> getNameFromUserId(Authentication authentication){
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            String username = userDetails.getUsername();
            Long userId = userService.getUserIdByUsername(username);
            ProfileModel profile = profileService.getUserDetailsByUserId(userId);

            if (profile != null) {
                return ResponseEntity.ok(profile.getName());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/contactUs")
    public ResponseEntity<ContactUs> saveContactUsDetails(@RequestBody ContactUs contactUsDetails,
                                          Authentication authentication){
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            String username = userDetails.getUsername();
            Long userId = userService.getUserIdByUsername(username);
            contactUsDetails.setUserId(userId);

            return ResponseEntity.ok(profileService.saveContactUsDetails(contactUsDetails));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/feedback")
    public ResponseEntity<Feedback> saveFeedback(Authentication authentication,
                                 @RequestBody Feedback feedback){

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            String username = userDetails.getUsername();
            Long userId = userService.getUserIdByUsername(username);
            feedback.setUserId(userId);

            return ResponseEntity.ok(profileService.saveFeedback(feedback));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
