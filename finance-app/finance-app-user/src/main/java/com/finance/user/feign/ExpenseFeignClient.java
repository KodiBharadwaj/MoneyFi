package com.finance.user.feign;


import com.finance.user.dto.ExpenseModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@FeignClient("FINANCE-APP-EXPENSE")
public interface ExpenseFeignClient {

    @PostMapping("/api/expense")
    public ResponseEntity<ExpenseModel> saveExpense(@RequestBody ExpenseModel expense);

    @GetMapping("/api/expense/{userId}")
    public ResponseEntity<List<ExpenseModel>> getAllExpenses(@PathVariable("userId") int userId);

    @GetMapping("api/expense/{userId}/{month}/{year}/{deleteStatus}")
    public ResponseEntity<List<ExpenseModel>> getAllExpensesByDate(@PathVariable("userId") int userId, @PathVariable("month") int month, @PathVariable("year") int year, @PathVariable("deleteStatus") boolean deleteStatus);

    @GetMapping("api/expense/{userId}/{year}/{deleteStatus}")
    public ResponseEntity<List<ExpenseModel>> getAllExpensesByYear(@PathVariable("userId") int userId, @PathVariable("year") int year, @PathVariable("deleteStatus") boolean deleteStatus);

    @GetMapping("/api/expense/{userId}/monthlyTotalExpensesList/{year}")
    public List<Double> getMonthlyExpenses(@PathVariable("userId") int userId, @PathVariable("year") int year);

    @PutMapping("/api/expense/{id}")
    public ExpenseModel updateExpense(@PathVariable("id") int id, @RequestBody ExpenseModel expense);

    @DeleteMapping("/api/expense/{id}")
    public void deleteExpenseById(@PathVariable("id") int id);

}
