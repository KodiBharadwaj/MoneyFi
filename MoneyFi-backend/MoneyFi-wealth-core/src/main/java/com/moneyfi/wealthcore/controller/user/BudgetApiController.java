package com.moneyfi.wealthcore.controller.user;

import com.moneyfi.wealthcore.model.budget.BudgetModel;
import com.moneyfi.wealthcore.security.JwtService;
import com.moneyfi.wealthcore.service.budget.BudgetService;
import com.moneyfi.wealthcore.service.budget.dto.request.AddBudgetDto;
import com.moneyfi.wealthcore.service.budget.dto.response.BudgetDetailsDto;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/wealth-core/budget")
@PreAuthorize("hasRole('USER')")
@Validated
@RequiredArgsConstructor
public class BudgetApiController {

    private final BudgetService budgetService;
    private final JwtService jwtService;

    @Operation(summary = "Api to add the budget")
    @PostMapping("/save")
    public void saveBudget(@RequestHeader("Authorization") String authHeader,
                           @RequestBody @Valid List<AddBudgetDto> budgetList) {
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        budgetService.saveBudget(budgetList, userId);
    }

    @Operation(summary = "Api to get budget of a user")
    @GetMapping("/{category}/{month}/{year}/get")
    public ResponseEntity<List<BudgetDetailsDto>> getAllBudgetsByUserIdAndCategory(@RequestHeader("Authorization") String authHeader,
                                                                                   @NotBlank @PathVariable(value = "category") String category,
                                                                                   @NotNull @PathVariable(value = "month") int month,
                                                                                   @NotNull @PathVariable(value = "year") int year) {
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return ResponseEntity.ok(budgetService.getAllBudgetsByUserIdAndCategory(userId, month, year, category));
    }

    @Operation(summary = "Api to get the budget status/progress")
    @GetMapping("/budgetProgress/{month}/{year}")
    public BigDecimal budgetProgress(@RequestHeader("Authorization") String authHeader,
                                     @NotNull @PathVariable(value = "month") int month,
                                     @NotNull @PathVariable(value = "year") int year){
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return budgetService.budgetProgress(userId, month, year);
    }

    @Operation(summary = "Api to update the budget")
    @PutMapping("/update")
    public void updateBudget(@RequestHeader("Authorization") String authHeader,
                             @RequestBody List<BudgetModel> budgetList) {
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
