package com.moneyfi.wealthcore.controller.common;

import com.moneyfi.constants.dto.CategoryResponseDto;
import com.moneyfi.wealthcore.service.common.CommonService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wealth-core/common")
@RequiredArgsConstructor
public class CommonController {
    
    private final CommonService commonService;
    
    @Operation(summary = "Api to get the category list based on type")
    @PostMapping("/category-list/get")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<List<CategoryResponseDto>> getCategoryWiseList(@RequestBody List<String> type) {
        return ResponseEntity.ok(commonService.getCategoryWiseList(type));
    }
}
