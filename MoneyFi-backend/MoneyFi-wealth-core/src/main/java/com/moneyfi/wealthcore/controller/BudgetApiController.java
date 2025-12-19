package com.moneyfi.wealthcore.controller;

import com.moneyfi.wealthcore.config.JwtService;
import com.moneyfi.wealthcore.model.BudgetModel;
import com.moneyfi.wealthcore.service.budget.BudgetService;
import com.moneyfi.wealthcore.service.budget.dto.request.AddBudgetDto;
import com.moneyfi.wealthcore.service.budget.dto.response.BudgetDetailsDto;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/wealth-core/budget")
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
}
