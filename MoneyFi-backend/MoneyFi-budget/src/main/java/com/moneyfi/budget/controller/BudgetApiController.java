package com.moneyfi.budget.controller;

import com.moneyfi.budget.config.JwtService;
import com.moneyfi.budget.model.BudgetModel;
import com.moneyfi.budget.service.BudgetService;
import com.moneyfi.budget.service.dto.request.AddBudgetDto;
import com.moneyfi.budget.service.dto.response.BudgetDetailsDto;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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

    @Operation(summary = "Method to add the budget")
    @PostMapping("/saveBudget")
    public void saveBudget(@RequestBody List<AddBudgetDto> budgetList,
                           @RequestHeader("Authorization") String authHeader) {
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        budgetService.saveBudget(budgetList, userId);
    }

    @Operation(summary = "Method to get budget of a user")
    @GetMapping("/getBudgetDetails/{category}/{month}/{year}")
    public ResponseEntity<List<BudgetDetailsDto>> getAllBudgetsByUserIdAndCategory(@RequestHeader("Authorization") String authHeader,
                                                                                   @PathVariable("category") String category,
                                                                                   @PathVariable("month") int month,
                                                                                   @PathVariable("year") int year) {
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        List<BudgetDetailsDto> list = budgetService.getAllBudgetsByUserIdAndCategory(userId, month, year, category);
        if (!list.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(list); // 200
        } else {
            return ResponseEntity.noContent().build(); // 204
        }
    }

    @Operation(summary = "Method to get the budget status/progress")
    @GetMapping("/budgetProgress/{month}/{year}")
    public BigDecimal budgetProgress(@RequestHeader("Authorization") String authHeader,
                                     @PathVariable("month") int month,
                                     @PathVariable("year") int year){
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return budgetService.budgetProgress(userId, month, year);
    }

    @Operation(summary = "Method to update the budget")
    @PutMapping("/updateBudget")
    public void updateBudget(@RequestBody List<BudgetModel> budgetList,
                                                    @RequestHeader("Authorization") String authHeader) {
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        budgetService.updateBudget(userId, budgetList);
    }

}
