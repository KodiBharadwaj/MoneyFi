package com.moneyfi.user.controller;

import com.moneyfi.user.service.common.UserCommonService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user-service/user-admin")
public class AdminAndUserCommonController {

    private final UserCommonService userCommonService;

    AdminAndUserCommonController(UserCommonService userCommonService) {
        this.userCommonService = userCommonService;
    }

    @Operation(summary = "Api end point to test")
    @GetMapping("/test")
    public String testFunction(){
        return "method entered";
    }

    @Operation(summary = "Api to get reasons for respected reason codes")
    @GetMapping("/reasons-dialog/get")
    public ResponseEntity<List<String>> getReasonsForDialogForUser(@RequestParam("code") int reasonCode){
        List<String> responseList = userCommonService.getReasonsForDialogForUser(reasonCode);
        return !responseList.isEmpty() ? ResponseEntity.status(HttpStatus.OK).body(responseList) :
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
}