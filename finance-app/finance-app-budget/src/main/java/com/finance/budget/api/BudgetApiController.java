package com.finance.budget.api;

import com.finance.budget.model.BudgetModel;
import com.finance.budget.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/budget")
public class BudgetApiController {

    @Autowired
    private BudgetService budgetService;

    @Operation(summary = "Method to add the budget")
    @PostMapping("/{userId}")
    public ResponseEntity<BudgetModel> saveBudget(@RequestBody BudgetModel budget,
                                                  @PathVariable("userId") int userId) {
        budget.setUserId(userId);
        BudgetModel createdBudget = budgetService.save(budget);
        if (createdBudget != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBudget); // 201
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null); // 409
        }
    }

    @Operation(summary = "Method to get budget of a user")
    @GetMapping("/{userId}")
    public ResponseEntity<List<BudgetModel>> getAllBudgets(@PathVariable("userId") int userId) {
        List<BudgetModel> list = budgetService.getAllBudgets(userId);
        if (!list.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(list); // 200
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // 404
        }
    }

    @Operation(summary = "Method to get the budget status/progress")
    @GetMapping("/{userId}/budgetProgress/{month}/{year}")
    public Double budgetProgress(@PathVariable("userId") int userId,
                                 @PathVariable("month") int month,
                                 @PathVariable("year") int year){
        return budgetService.budgetProgress(userId, month, year);
    }

    @Operation(summary = "Method to update the budget")
    @PutMapping("/{id}")
    public ResponseEntity<BudgetModel> updateBudget(@PathVariable("id") int id, @RequestBody BudgetModel budget) {
        BudgetModel updatedBudget = budgetService.update(id, budget);
        System.out.println(updatedBudget);
        if (updatedBudget != null) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(updatedBudget); // 202
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

}
