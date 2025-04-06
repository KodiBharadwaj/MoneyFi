package com.moneyfi.expense.controller;

import com.moneyfi.expense.model.ExpenseModel;
import com.moneyfi.expense.repository.ExpenseRepository;
import com.moneyfi.expense.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/expense")
public class ExpenseApiController {

    private final ExpenseService expenseService;
    private final ExpenseRepository expenseRepository;

    public ExpenseApiController(ExpenseService expenseService,
                                ExpenseRepository expenseRepository){
        this.expenseService = expenseService;
        this.expenseRepository = expenseRepository;
    }

    @Operation(summary = "Method to add the expense transaction")
    @PostMapping("/{userId}")
    public ResponseEntity<ExpenseModel> saveExpense(@RequestBody ExpenseModel expense,
                                                    @PathVariable("userId") Long userId) {
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
    public ResponseEntity<List<ExpenseModel>> getAllExpenses(@PathVariable("userId") Long userId) {
        List<ExpenseModel> list = expenseService.getAllexpenses(userId);
        return ResponseEntity.status(HttpStatus.OK).body(list); // 200
    }

    @Operation(summary = "Method to get all the expense details in a particular month and in a particular year")
    @GetMapping("/{userId}/{month}/{year}/{deleteStatus}")
    public ResponseEntity<List<ExpenseModel>> getAllExpensesByDate(@PathVariable("userId") Long userId,
                                                                   @PathVariable("month") int month,
                                                                   @PathVariable("year") int year,
                                                                   @PathVariable("deleteStatus") boolean deleteStatus){
        return ResponseEntity.status(HttpStatus.OK).body(expenseService.getAllexpensesByDate(userId, month, year, deleteStatus));
    }

    @Operation(summary = "Method to generate the Excel report for the Monthly expenses of a user")
    @GetMapping("/{userId}/{month}/{year}/generateMonthlyReport")
    public ResponseEntity<byte[]> getMonthlyExpenseReport(@PathVariable("userId") Long userId,
                                                          @PathVariable("month") int month,
                                                          @PathVariable("year") int year) throws IOException {

        byte[] excelData = expenseService.generateMonthlyExcelReport(userId, month, year);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Monthly expense report.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelData);
    }

    @Operation(summary = "Method to get all the expense details in a particular year")
    @GetMapping("/{userId}/{year}/{deleteStatus}")
    public ResponseEntity<List<ExpenseModel>> getAllExpensesByYear(@PathVariable("userId") Long userId,
                                                                   @PathVariable("year") int year,
                                                                   @PathVariable("deleteStatus") boolean deleteStatus){
        return ResponseEntity.status(HttpStatus.OK).body(expenseService.getAllexpensesByYear(userId, year, deleteStatus));
    }

    @Operation(summary = "Method to generate the Excel report for the Yearly Expenses of a user")
    @GetMapping("/{userId}/{year}/generateYearlyReport")
    public ResponseEntity<byte[]> getYearlyExpenseReport(@PathVariable("userId") Long userId,
                                                         @PathVariable("year") int year) throws IOException {

        byte[] excelData = expenseService.generateYearlyExcelReport(userId, year);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Yearly expense report.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelData);
    }

    @Operation(summary = "Method to get the total expense amount in a particular month and in a particular year")
    @GetMapping("/{userId}/totalExpense/{month}/{year}")
    public BigDecimal getTotalExpenseByMonthAndYear(@PathVariable("userId") Long userId,
                                                    @PathVariable("month") int month,
                                                    @PathVariable("year") int year){
        return expenseService.getTotalExpenseInMonthAndYear(userId, month, year);
    }

    @Operation(summary = "Method to get the list of total expense amount of all months in a particular year")
    @GetMapping("/{userId}/monthlyTotalExpensesList/{year}")
    public List<BigDecimal> getMonthlyTotals(@PathVariable("userId") Long userId,
                                         @PathVariable("year") int year) {
        return expenseService.getMonthlyExpenses(userId, year);
    }

    @Operation(summary = "Method to get the total expense amount up to previous month (excludes current month)")
    @GetMapping("/{userId}/totalExpensesUpToPreviousMonth/{month}/{year}")
    public BigDecimal getTotalExpensesUpToPreviousMonth(
            @PathVariable("userId") Long userId,
            @PathVariable("month") int month,
            @PathVariable("year") int year) {

        return expenseService.getTotalExpensesUpToPreviousMonth(userId, month, year);
    }

    @Operation(summary = "Method to find the total savings/ remaining amount in a month")
    @GetMapping("/{userId}/totalSavings/{month}/{year}")
    public BigDecimal getTotalSavingsByMonthAndDate(@PathVariable("userId") Long userId,
                                                @PathVariable("month") int month,
                                                @PathVariable("year") int year){

        return expenseService.getTotalSavingsByMonthAndDate(userId, month, year);
    }

    @Operation(summary = "Method to get the list of cumulative savings monthly wise in a year")
    @GetMapping("/{userId}/monthlyCumulativeSavingsInYear/{year}")
    public List<BigDecimal> getCumulativeMonthlySavings(@PathVariable("userId") Long userId, @PathVariable("year") int year){

        return expenseService.getCumulativeMonthlySavings(userId, year);
    }

    @Operation(summary = "Method to update the expense details")
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseModel> updateExpense(@PathVariable("id") Long id,
                                                      @RequestBody ExpenseModel expense) {
        ExpenseModel expenseModel = expenseRepository.findById(id).orElse(null);
        if(expenseModel != null){
            if(expenseModel.getAmount() == expense.getAmount() &&
                    expenseModel.getCategory().equals(expense.getCategory()) &&
                    expenseModel.getDescription().equals(expense.getDescription()) &&
                    expenseModel.getDate().equals(expense.getDate()) &&
                    expenseModel.isRecurring() == expense.isRecurring()){
                return ResponseEntity.noContent().build(); // 204
            }
        }
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
    public ResponseEntity<Void> deleteExpenseById(@PathVariable("id") Long id) {
        boolean isDeleted = expenseService.deleteExpenseById(id);
        if (isDeleted) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // 204: No Content
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404: Not Found
        }
    }


}
