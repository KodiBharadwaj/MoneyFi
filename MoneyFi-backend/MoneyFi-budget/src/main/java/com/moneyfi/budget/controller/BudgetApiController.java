package com.moneyfi.budget.controller;

import com.moneyfi.budget.config.JwtService;
import com.moneyfi.budget.model.BudgetModel;
import com.moneyfi.budget.service.BudgetService;
import com.moneyfi.budget.service.dto.request.AddBudgetDto;
import com.moneyfi.budget.service.dto.response.BudgetDetailsDto;
import com.moneyfi.budget.service.dto.response.SpendingAnalysisResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/budget")
public class BudgetApiController {

    private final BudgetService budgetService;
    private final JwtService jwtService;

    public BudgetApiController(BudgetService budgetService,
                               JwtService jwtService){
        this.budgetService = budgetService;
        this.jwtService = jwtService;
    }

    @Operation(summary = "Api to add the budget")
    @PostMapping("/save")
    public void saveBudget(@RequestBody List<AddBudgetDto> budgetList,
                           @RequestHeader("Authorization") String authHeader) {
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        budgetService.saveBudget(budgetList, userId);
    }

    @Operation(summary = "Api to get budget of a user")
    @GetMapping("/{category}/{month}/{year}/get")
    public ResponseEntity<List<BudgetDetailsDto>> getAllBudgetsByUserIdAndCategory(@RequestHeader("Authorization") String authHeader,
                                                                                   @PathVariable("category") String category,
                                                                                   @PathVariable("month") int month,
                                                                                   @PathVariable("year") int year) {
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return ResponseEntity.ok(budgetService.getAllBudgetsByUserIdAndCategory(userId, month, year, category));
    }

    @Operation(summary = "Api to get the budget status/progress")
    @GetMapping("/budgetProgress/{month}/{year}")
    public BigDecimal budgetProgress(@RequestHeader("Authorization") String authHeader,
                                     @PathVariable("month") int month,
                                     @PathVariable("year") int year){
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return budgetService.budgetProgress(userId, month, year);
    }

    @Operation(summary = "Api to update the budget")
    @PutMapping("/update")
    public void updateBudget(@RequestBody List<BudgetModel> budgetList,
                             @RequestHeader("Authorization") String authHeader) {
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        budgetService.updateBudget(userId, budgetList);
    }

    @Operation(summary = "Api to delete the budget")
    @DeleteMapping("/delete")
    public void deleteBudget(@RequestHeader("Authorization") String authHeader) {
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        budgetService.deleteBudget(userId);
    }

    @Operation(summary = "Api to get the user spending analysis in a particular time period")
    @GetMapping("/spending-analysis")
    public SpendingAnalysisResponseDto getUserSpendingAnalysisByBudgetCategories(@RequestHeader("Authorization") String authHeader,
                                                                                 @RequestParam LocalDate fromDate,
                                                                                 @RequestParam LocalDate toDate){
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return budgetService.getUserSpendingAnalysisByBudgetCategories(userId, fromDate, toDate, authHeader);
    }

    @Operation(summary = "Api to get the user spending analysis in pdf format in a particular time period")
    @GetMapping("/spending-analysis/report")
    public ResponseEntity<byte[]> getUserSpendingAnalysisByBudgetCategoriesPdf(@RequestHeader("Authorization") String authHeader,
                                                                               @RequestParam LocalDate fromDate,
                                                                               @RequestParam LocalDate toDate){
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        byte[] pdfBytes = budgetService.getUserSpendingAnalysisByBudgetCategoriesPdf(userId, fromDate, toDate, authHeader);
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
        return budgetService.getUserSpendingAnalysisByBudgetCategoriesPdfEmail(userId, fromDate, toDate, authHeader);
    }

}
