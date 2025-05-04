package com.finance.user.feign;


import com.finance.user.dto.BudgetModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@FeignClient("FINANCE-APP-BUDGET")
public interface BudgetFeignClient {

    @PostMapping("/api/budget/addBudget")
    public ResponseEntity<BudgetModel> addToBudget(@RequestBody BudgetModel budget);

    @GetMapping("/api/budget/{userId}")
    public ResponseEntity<List<BudgetModel>> getAllBudgets(@PathVariable("userId") int userId);

    @PutMapping("/api/budget/{userId}/category")
    public ResponseEntity<BudgetModel> updateBudget(@PathVariable("userId") int userId, @RequestParam("name") String category, @RequestBody BudgetModel budget);

    @DeleteMapping("/api/budget/{userId}/category")
    public void deleteBudgetByCategory(@PathVariable("userId") int userId, @RequestParam("name") String category);

}
