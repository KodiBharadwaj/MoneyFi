package com.moneyfi.wealthcore.controller;

import com.moneyfi.wealthcore.service.admin.dto.response.CategoryResponseDto;
import com.moneyfi.wealthcore.service.common.CommonService;
import com.moneyfi.wealthcore.service.wealthcore.WealthCoreService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wealth-core/common")
public class CommonController {
    
    private final CommonService commonService;
    
    public CommonController(CommonService commonService) {
        this.commonService = commonService;
    }

    @Operation(summary = "Api to get the category list based on type")
    @PostMapping("/category-list/get")
    public ResponseEntity<List<CategoryResponseDto>> getCategoryWiseList(@RequestBody List<String> type) {
        return ResponseEntity.ok(commonService.getCategoryWiseList(type));
    }
}
