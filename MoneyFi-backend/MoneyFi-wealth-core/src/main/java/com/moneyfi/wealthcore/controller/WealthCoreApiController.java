package com.moneyfi.wealthcore.controller;

import com.moneyfi.wealthcore.config.JwtService;
import com.moneyfi.wealthcore.service.budget.BudgetService;
import com.moneyfi.wealthcore.service.budget.dto.response.SpendingAnalysisResponseDto;
import com.moneyfi.wealthcore.service.wealthcore.WealthCoreService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/wealth-core/user")
public class WealthCoreApiController {

    private final WealthCoreService wealthCoreService;
    private final JwtService jwtService;

    public WealthCoreApiController(WealthCoreService wealthCoreService,
                                   JwtService jwtService){
        this.wealthCoreService = wealthCoreService;
        this.jwtService = jwtService;
    }

    @Operation(summary = "Api to get the user spending analysis in a particular time period")
    @GetMapping("/spending-analysis")
    public SpendingAnalysisResponseDto getUserSpendingAnalysisByBudgetCategories(@RequestHeader("Authorization") String authHeader,
                                                                                 @RequestParam LocalDate fromDate,
                                                                                 @RequestParam LocalDate toDate){
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return wealthCoreService.getUserSpendingAnalysisByBudgetCategories(userId, fromDate, toDate, authHeader);
    }

    @Operation(summary = "Api to get the user spending analysis in pdf format in a particular time period")
    @GetMapping("/spending-analysis/report")
    public ResponseEntity<byte[]> getUserSpendingAnalysisByBudgetCategoriesPdf(@RequestHeader("Authorization") String authHeader,
                                                                               @RequestParam LocalDate fromDate,
                                                                               @RequestParam LocalDate toDate){
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        byte[] pdfBytes = wealthCoreService.getUserSpendingAnalysisByBudgetCategoriesPdf(userId, fromDate, toDate, authHeader);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=spending-analysis.pdf")
                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                .body(pdfBytes);
    }

    @Operation(summary = "Api to get the user spending analysis in pdf format and send email to user in a particular time period")
    @GetMapping("/spending-analysis/report-email")
    public ResponseEntity<String> getUserSpendingAnalysisByBudgetCategoriesPdfEmail(@RequestHeader("Authorization") String authHeader,
                                                                                    @RequestParam LocalDate fromDate,
                                                                                    @RequestParam LocalDate toDate){
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return wealthCoreService.getUserSpendingAnalysisByBudgetCategoriesPdfEmail(userId, fromDate, toDate, authHeader);
    }
}
