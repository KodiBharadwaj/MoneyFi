package com.moneyfi.user.controller;

import com.moneyfi.user.service.common.UserCommonService;
import com.moneyfi.user.service.common.dto.request.AccountRetrieveRequestDto;
import com.moneyfi.user.service.common.dto.request.ForgotUsernameDto;
import com.moneyfi.user.service.common.dto.request.HelpCenterContactUsRequestDto;
import com.moneyfi.user.service.common.dto.request.NameChangeRequestDto;
import com.moneyfi.user.service.common.dto.response.UserRequestStatusDto;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user-service/common")
public class CommonController {

    private final UserCommonService userCommonService;

    public CommonController(UserCommonService userCommonService){
        this.userCommonService = userCommonService;
    }

    @Operation(summary = "Api end point to test")
    @GetMapping("/test")
    public String testFunction(){
        return "method entered";
    }

    @Operation(summary = "Api to send reference number to user for account retrieval/name change")
    @GetMapping("/{requestStatus}/{email}/reference-number-request")
    public Map<Boolean, String> requestReferenceNumber(@PathVariable("requestStatus") String requestStatus,
                                                       @PathVariable("email") String email){
        return userCommonService.sendReferenceRequestNumberEmail(requestStatus, email);
    }

    @Operation(summary = "Api request to get account unblock/retrieve")
    @PostMapping("/account-retrieve-request")
    public void accountUnblockOrRetrieveRequestByUser(@Valid @RequestBody AccountRetrieveRequestDto requestDto){
        userCommonService.accountReactivateRequestByUser(requestDto);
    }

    @Operation(summary = "Api request to save user details to change name of the user")
    @PostMapping("/name-change-request")
    public void nameChangeRequestByUser(@Valid @RequestBody NameChangeRequestDto requestDto){
        userCommonService.nameChangeRequestByUser(requestDto);
    }

    @Operation(summary = "Api to check the status of user request using reference number")
    @GetMapping("/track-user-request")
    public ResponseEntity<UserRequestStatusDto> trackUserRequestUsingReferenceNumber(@RequestParam("ref") String referenceNumber){
        UserRequestStatusDto userRequestStatusDto = userCommonService.trackUserRequestUsingReferenceNumber(referenceNumber);
        if(userRequestStatusDto != null){
            return ResponseEntity.status(HttpStatus.OK).body(userRequestStatusDto);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @Operation(summary = "Api to get reasons for respected reason codes")
    @GetMapping("/reasons-dialog/get")
    public ResponseEntity<List<String>> getReasonsForDialogForUser(@RequestParam("code") int reasonCode){
        List<String> responseList = userCommonService.getReasonsForDialogForUser(reasonCode);
        return !responseList.isEmpty() ? ResponseEntity.status(HttpStatus.OK).body(responseList) :
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @Operation(summary = "Api to send the contact us/help center details to admin via mail")
    @PostMapping("/contact-us")
    public void sendContactUsDetailsToAdmin(@RequestBody HelpCenterContactUsRequestDto requestDto) {
        userCommonService.sendContactUsDetailsToAdmin(requestDto);
    }

    @Operation(summary = "Api to return username when user forgets username")
    @PostMapping("/username/forgot")
    public ResponseEntity<Boolean> forgotUsername(@RequestBody ForgotUsernameDto userDetails){
        return ResponseEntity.ok(userCommonService.getUsernameByDetails(userDetails));
    }

    @Operation(summary = "Api to upload profile pic to AWS S3")
    @PostMapping("/{username}/{userId}/profile-picture/upload")
    public ResponseEntity<String> uploadUserProfilePictureToS3(@PathVariable("username") String username,
                                                               @PathVariable("userId") Long userId,
                                                               @RequestParam(value = "file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(userCommonService.uploadUserProfilePictureToS3(username, userId, file));
    }
}
