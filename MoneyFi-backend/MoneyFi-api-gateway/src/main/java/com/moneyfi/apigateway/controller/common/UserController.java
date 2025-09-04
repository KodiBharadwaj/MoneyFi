package com.moneyfi.apigateway.controller.common;

import com.moneyfi.apigateway.model.auth.UserAuthModel;
import com.moneyfi.apigateway.service.common.dto.request.AccountRetrieveRequestDto;
import com.moneyfi.apigateway.service.common.dto.request.NameChangeRequestDto;
import com.moneyfi.apigateway.service.common.dto.response.UserRequestStatusDto;
import com.moneyfi.apigateway.service.jwtservice.JwtService;
import com.moneyfi.apigateway.service.common.UserCommonService;
import com.moneyfi.apigateway.service.userservice.UserService;
import com.moneyfi.apigateway.service.userservice.dto.ForgotUsernameDto;
import com.moneyfi.apigateway.service.userservice.dto.RemainingTimeCountDto;
import com.moneyfi.apigateway.service.userservice.dto.UserProfile;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
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
import java.util.Map;

@RestController
@CrossOrigin("http://localhost:4200")
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;
    private final UserCommonService userCommonService;

@Autowired
private DataSource dataSource;
    public UserController(UserService userService,
                          JwtService jwtService,
                          UserCommonService resetPassword){
        this.userService = userService;
        this.jwtService = jwtService;
        this.userCommonService = resetPassword;
    }

    @Operation(summary = "Api end point to test")
    @GetMapping("/test")
    public String testFunction(){
        return "method entered";
    }

    @Operation(summary = "Method for the user registration/signup")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserProfile userProfile) {

        UserAuthModel user = userService.registerUser(userProfile);
        if(user == null){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists"); //409
        } else {
            return ResponseEntity.ok(jwtService.generateToken(user)); // 200
        }
    }

    @Operation(summary = "Method for the user to login")
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody UserAuthModel userAuthModel) {
        return userService.login(userAuthModel);
    }

    @Operation(summary = "Method for password forgot")
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        return ResponseEntity.ok(userCommonService.forgotPassword(email));
    }

    @Operation(summary = "Method for verification of code/otp")
    @PostMapping("/verify-code")
    public String verifyCode(@RequestParam String email,
                             @RequestParam String code) {
        boolean isValid = userCommonService.verifyCode(email, code);
        if (isValid) {
            return "Verification successful!";
        } else {
            throw new IllegalArgumentException("Invalid verification code");
        }
    }

    @Operation(summary = "Method to update the user's password")
    @PutMapping("/update-password")
    public String updatePassword(@RequestParam String email,
                                 @RequestParam String password){
        return userCommonService.updatePassword(email,password);
    }

    @Operation(summary = "Method to send Otp for user verification during signup")
    @GetMapping("/sendOtpForSignup/{email}/{name}")
    public ResponseEntity<String> sendOtpForSignup(@PathVariable("email") String email,
                                                   @PathVariable("name") String name){

        return ResponseEntity.ok(userService.sendOtpForSignup(email, name));
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

    @Operation(summary = "Api request to get account unblock")
    @PostMapping("/account-retrieve-request")
    public void accountUnblockRequestByUser(@RequestBody AccountRetrieveRequestDto requestDto){
        userCommonService.accountUnblockRequestByUser(requestDto);
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
