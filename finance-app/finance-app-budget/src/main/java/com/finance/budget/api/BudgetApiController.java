package com.finance.budget.api;

import com.finance.budget.model.BudgetModel;
import com.finance.budget.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budget")
public class BudgetApiController {

    @Autowired
    private BudgetService budgetService;

    @PostMapping
    public ResponseEntity<BudgetModel> saveBudget(@RequestBody BudgetModel budget) {
        BudgetModel createdBudget = budgetService.save(budget);
        if (createdBudget != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBudget); // 201
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null); // 409
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<BudgetModel>> getAllBudgets(@PathVariable("userId") int userId) {
        List<BudgetModel> list = budgetService.getAllBudgets(userId);
        if (!list.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(list); // 200
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // 404
        }
    }

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
