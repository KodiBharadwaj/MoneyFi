package com.moneyfi.apigateway.controller.common;

import com.moneyfi.apigateway.service.common.dto.request.AccountRetrieveRequestDto;
import com.moneyfi.apigateway.service.common.dto.request.NameChangeRequestDto;
import com.moneyfi.apigateway.service.common.dto.response.UserRequestStatusDto;
import com.moneyfi.apigateway.service.common.UserCommonService;
import com.moneyfi.apigateway.service.userservice.UserService;
import com.moneyfi.apigateway.service.userservice.dto.request.ForgotUsernameDto;
import com.moneyfi.apigateway.service.userservice.dto.request.UserLoginDetailsRequestDto;
import com.moneyfi.apigateway.service.userservice.dto.response.RemainingTimeCountDto;
import com.moneyfi.apigateway.service.userservice.dto.request.UserProfile;
import com.moneyfi.apigateway.util.enums.LoginMode;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.sql.DataSource;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;
    private final UserCommonService userCommonService;

@Autowired
private DataSource dataSource;
    public UserController(UserService userService,
                          UserCommonService resetPassword){
        this.userService = userService;
        this.userCommonService = resetPassword;
    }

    @Operation(summary = "Api end point to test")
    @GetMapping("/test")
    public String testFunction(){
        return "method entered";
    }

    @Operation(summary = "Api end point for the user registration/signup using email password mode")
    @PostMapping("/register")
    public void registerUser(@Valid @RequestBody UserProfile userProfile) {
        userService.registerUser(userProfile, LoginMode.EMAIL_PASSWORD.name(), null);
    }

    @Operation(summary = "Api to send Otp for user verification during signup")
    @GetMapping("/send-otp/signup")
    public ResponseEntity<String> sendOtpForSignup(@RequestParam("email") String email,
                                                   @RequestParam("name") String name){

        return ResponseEntity.ok(userService.sendOtpForSignup(email, name));
    }

    @Operation(summary = "Api end point for user to login")
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginUserViaEmailPasswordMode(@Valid @RequestBody UserLoginDetailsRequestDto requestDto) {
        try {
            return userService.login(requestDto);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Api end point to send otp for forgot password")
    @GetMapping("/forgot-password/get-otp")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        return ResponseEntity.ok(userCommonService.forgotPassword(email));
    }

    @Operation(summary = "Api end point for verification of code/otp during forgot password process")
    @GetMapping("/forgot-password/verify-otp")
    public ResponseEntity<String> verifyCode(@RequestParam String email,
                                             @RequestParam String code) {
        return ResponseEntity.ok(userCommonService.verifyCode(email, code));
    }

    @Operation(summary = "Api end point to update the user's password for forgot password")
    @PutMapping("/forgot-password/update-password")
    public ResponseEntity<String> updatePassword(@RequestParam String email,
                                                 @RequestParam String password){
        return ResponseEntity.ok(userCommonService.updatePassword(email, password));
    }

    @Operation(summary = "Method to check the eligibility for next otp")
    @GetMapping("/checkOtpActive/{email}")
    public RemainingTimeCountDto checkOtpActiveMethod(@PathVariable("email") String email){
        return userService.checkOtpActiveMethod(email);
    }

    @Operation(summary = "Method to check the otp entered correct or not during user creation")
    @GetMapping("/checkOtp/{email}/{inputOtp}")
    public boolean checkEnteredOtp(@PathVariable("email") String email,
                                   @PathVariable("inputOtp") String inputOtp){
        return userService.checkEnteredOtp(email, inputOtp);
    }

    @Operation(summary = "Method to return username when user forgets username")
    @PostMapping("/forgotUsername")
    public boolean forgotUsername(@RequestBody ForgotUsernameDto userDetails){
        return userService.getUsernameByDetails(userDetails);
    }

    @Operation(summary = "Api to send reference number to the user for account retrieval/name change")
    @GetMapping("/{requestStatus}/{email}/reference-number-request")
    public Map<Boolean, String> requestReferenceNumber(@PathVariable("requestStatus") String requestStatus,
                                                       @PathVariable("email") String email){
        return userCommonService.sendReferenceRequestNumberEmail(requestStatus, email);
    }

    @Operation(summary = "Api request to get account unblock/retrieve")
    @PostMapping("/account-retrieve-request")
    public void accountUnblockOrRetrieveRequestByUser(@RequestBody AccountRetrieveRequestDto requestDto){
        userCommonService.accountReactivateRequestByUser(requestDto);
    }

    @Operation(summary = "Api request to save the user details to change name of the user")
    @PostMapping("/name-change-request")
    public void nameChangeRequestByUser(@RequestBody NameChangeRequestDto requestDto){
        userCommonService.nameChangeRequestByUser(requestDto);
    }

    @Operation(summary = "Api to check the status of the user request using reference number")
    @GetMapping("/track-user-request")
    public ResponseEntity<UserRequestStatusDto> trackUserRequestUsingReferenceNumber(@RequestParam("ref") String referenceNumber){
        UserRequestStatusDto userRequestStatusDto = userCommonService.trackUserRequestUsingReferenceNumber(referenceNumber);
        if(userRequestStatusDto != null){
            return ResponseEntity.status(HttpStatus.OK).body(userRequestStatusDto);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @Operation(summary = "Api to get the reasons for the respected reason codes")
    @GetMapping("/reasons-dialog/get")
    public ResponseEntity<List<String>> getReasonsForDialogForUser(@RequestParam("code") int reasonCode){
        List<String> responseList = userCommonService.getReasonsForDialogForUser(reasonCode);
        return !responseList.isEmpty() ? ResponseEntity.status(HttpStatus.OK).body(responseList) :
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @GetMapping("/stream-large-data")
    public StreamingResponseBody streamLargeData(HttpServletResponse response) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        return outputStream -> {
            try (
                    Connection conn = dataSource.getConnection();
                    CallableStatement stmt = conn.prepareCall("exec getTestValuesForStreaming");
                    ResultSet rs = stmt.executeQuery();
                    PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
            ) {
                writer.print("["); // start of JSON array
                boolean first = true;

                while (rs.next()) {
                    if (!first) {
                        writer.print(",");
                    }
                    first = false;

                    // Build JSON manually or use Jackson
                    String json = String.format("{\"id\": %d, \"name\": \"%s\"}",
                            rs.getInt("id"),
                            rs.getString("name"));

                    writer.print(json);
                    writer.flush(); // send chunk immediately
                }

                writer.print("]"); // end of JSON array
                writer.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }


}
