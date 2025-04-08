package com.moneyfi.budget.controller;

import com.moneyfi.budget.model.BudgetModel;
import com.moneyfi.budget.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/budget")
public class BudgetApiController {

    private final BudgetService budgetService;

    public BudgetApiController(BudgetService budgetService){
        this.budgetService = budgetService;
    }

    @Operation(summary = "Method to add the budget")
    @PostMapping("/{userId}")
    public ResponseEntity<BudgetModel> saveBudget(@RequestBody BudgetModel budget,
                                                  @PathVariable("userId") Long userId) {
        budget.setUserId(userId);
        BudgetModel createdBudget = budgetService.save(budget);
        if (createdBudget != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBudget); // 201
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null); // 409
        }
    }

    @Operation(summary = "Method to get budget of a user")
    @GetMapping("/{userId}/{category}")
    public ResponseEntity<List<BudgetModel>> getAllBudgetsByUserIdAndCategory(@PathVariable("userId") Long userId,
                                                                              @PathVariable("category") String category) {
        List<BudgetModel> list = budgetService.getAllBudgetsByUserIdAndCategory(userId, category);
        if (!list.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(list); // 200
        } else {
            return ResponseEntity.noContent().build(); // 204
        }
    }

    @Operation(summary = "Method to get the budget status/progress")
    @GetMapping("/{userId}/budgetProgress/{month}/{year}")
    public BigDecimal budgetProgress(@PathVariable("userId") Long userId,
                                     @PathVariable("month") int month,
                                     @PathVariable("year") int year){
        return budgetService.budgetProgress(userId, month, year);
    }

    @Operation(summary = "Method to update the budget")
    @PutMapping("/{id}")
    public ResponseEntity<BudgetModel> updateBudget(@PathVariable("id") Long id, @RequestBody BudgetModel budget) {
        BudgetModel updatedBudget = budgetService.update(id, budget);
        System.out.println(updatedBudget);
        if (updatedBudget != null) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(updatedBudget); // 202
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

}
