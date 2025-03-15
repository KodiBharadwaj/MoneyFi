package com.finance.expense.api;

import com.finance.expense.dto.ExpenseDto;
import com.finance.expense.model.ExpenseModel;
import com.finance.expense.repository.ExpenseRepository;
import com.finance.expense.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/expense")
public class ExpenseApiController {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Operation(summary = "Method to add the expense transaction")
    @PostMapping("/{userId}")
    public ResponseEntity<ExpenseModel> saveExpense(@RequestBody ExpenseModel expense,
                                                    @PathVariable("userId") int userId) {
        expense.setUserId(userId);
        ExpenseModel createdExpense = expenseService.save(expense);
        if (createdExpense != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(createdExpense); // 201
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null); // 409
        }
    }

    @Operation(summary = "Method to get all the expenses of a particular user")
    @GetMapping("/{userId}")
    public ResponseEntity<List<ExpenseModel>> getAllExpenses(@PathVariable("userId") int userId) {
        List<ExpenseModel> list = expenseService.getAllexpenses(userId);
            return ResponseEntity.status(HttpStatus.OK).body(list); // 200
    }

    @Operation(summary = "Method to get all the expense details in a particular month and in a particular year")
    @GetMapping("/{userId}/{month}/{year}/{deleteStatus}")
    public ResponseEntity<List<ExpenseModel>> getAllExpensesByDate(@PathVariable("userId") int userId,
                                                                   @PathVariable("month") int month,
                                                                   @PathVariable("year") int year,
                                                                   @PathVariable("deleteStatus") boolean deleteStatus){
        return ResponseEntity.status(HttpStatus.OK).body(expenseService.getAllexpensesByDate(userId, month, year, deleteStatus));
    }

    @Operation(summary = "Method to get all the expense details in a particular year")
    @GetMapping("/{userId}/{year}/{deleteStatus}")
    public ResponseEntity<List<ExpenseModel>> getAllExpensesByYear(@PathVariable("userId") int userId,
                                                                   @PathVariable("year") int year,
                                                                   @PathVariable("deleteStatus") boolean deleteStatus){
        return ResponseEntity.status(HttpStatus.OK).body(expenseService.getAllexpensesByYear(userId, year, deleteStatus));
    }
    @Operation(summary = "Method to get the total expense amount in a particular month and in a particular year")
    @GetMapping("/{userId}/totalExpense/{month}/{year}")
    public Double getTotalExpenseByMonthAndYear(@PathVariable("userId") int userId, @PathVariable("month") int month, @PathVariable("year") int year){
        return expenseService.getTotalExpenseInMonthAndYear(userId, month, year);
    }

    @Operation(summary = "Method to get the list of total expense amount of all months in a particular year")
    @GetMapping("/{userId}/monthlyTotalExpensesList/{year}")
    public List<Double> getMonthlyTotals(@PathVariable("userId") int userId,
                                         @PathVariable("year") int year) {
        return expenseService.getMonthlyExpenses(userId, year);
    }

    @Operation(summary = "Method to get the total expense amount up to previous month (excludes current month)")
    @GetMapping("/{userId}/totalExpensesUpToPreviousMonth/{month}/{year}")
    public Double getTotalExpensesUpToPreviousMonth(
            @PathVariable("userId") int userId,
            @PathVariable("month") int month,
            @PathVariable("year") int year) {

        return expenseService.getTotalExpensesUpToPreviousMonth(userId, month, year);
    }

    @Operation(summary = "Method to find the total savings/ remaining amount in a month")
    @GetMapping("/{userId}/totalSavings/{month}/{year}")
    public Double getTotalSavingsByMonthAndDate(@PathVariable("userId") int userId,
                                                @PathVariable("month") int month,
                                                @PathVariable("year") int year){

        return expenseService.getTotalSavingsByMonthAndDate(userId, month, year);
    }

    @Operation(summary = "Method to get the list of cumulative savings monthly wise in a year")
    @GetMapping("/{userId}/monthlyCumulativeSavingsInYear/{year}")
    public List<Double> getCumulativeMonthlySavings(@PathVariable("userId") int userId, @PathVariable("year") int year){

        return expenseService.getCumulativeMonthlySavings(userId, year);
    }

    @Operation(summary = "Method to update the expense details")
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseModel> updateExpense(@PathVariable("id") int id, @RequestBody ExpenseModel expense) {
        ExpenseModel updatedExpense = expenseService.updateBySource(id, expense);
        if(updatedExpense!=null){
            return ResponseEntity.status(HttpStatus.CREATED).body(updatedExpense);
        }
        else{
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }

    @Operation(summary = "Method to delete the particular expense. Here which is typically soft delete")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpenseById(@PathVariable("id") int id) {
        boolean isDeleted = expenseService.deleteExpenseById(id);
        if (isDeleted) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // 204: No Content
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404: Not Found
        }
    }


}
