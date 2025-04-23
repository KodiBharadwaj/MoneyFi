package com.moneyfi.expense.controller;

import com.moneyfi.expense.config.JwtService;
import com.moneyfi.expense.model.ExpenseModel;
import com.moneyfi.expense.repository.ExpenseRepository;
import com.moneyfi.expense.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/expense")
public class ExpenseApiController {

    private final ExpenseService expenseService;
    private final ExpenseRepository expenseRepository;
    private final JwtService jwtService;

    public ExpenseApiController(ExpenseService expenseService,
                                ExpenseRepository expenseRepository,
                                JwtService jwtService){
        this.expenseService = expenseService;
        this.expenseRepository = expenseRepository;
        this.jwtService = jwtService;
    }

//    @GetMapping("/test")
//    public Object testFn(@RequestHeader("Authorization") String authHeader){
//        String token = authHeader.substring(7); // Remove "Bearer " from the start of the token
//        String username = jwtService.extractUserName(token);
//        return username;
//    }

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
    @GetMapping("/getExpenses")
    public ResponseEntity<List<ExpenseModel>> getAllExpenses(@RequestHeader("Authorization") String authHeader) {
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));

        List<ExpenseModel> list = expenseService.getAllExpenses(userId);
        return ResponseEntity.status(HttpStatus.OK).body(list); // 200
    }

    @Operation(summary = "Method to get all the expense details in a particular month and in a particular year")
    @GetMapping("/getExpenses/{month}/{year}/{category}/{deleteStatus}")
    public ResponseEntity<List<ExpenseModel>> getAllExpensesByMonthYearAndCategory(@RequestHeader("Authorization") String authHeader,
                                                                   @PathVariable("month") int month,
                                                                   @PathVariable("year") int year,
                                                                   @PathVariable("category") String category,
                                                                   @PathVariable("deleteStatus") boolean deleteStatus){
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));

        return ResponseEntity.status(HttpStatus.OK).body(expenseService.getAllExpensesByMonthYearAndCategory(userId, month, year, category, deleteStatus));
    }

    @Operation(summary = "Method to generate the Excel report for the Monthly expenses of a user")
    @GetMapping("/{month}/{year}/{category}/generateMonthlyReport")
    public ResponseEntity<byte[]> getMonthlyExpenseReport(@RequestHeader("Authorization") String authHeader,
                                                          @PathVariable("month") int month,
                                                          @PathVariable("category") String category,
                                                          @PathVariable("year") int year) throws IOException {

        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));

        byte[] excelData = expenseService.generateMonthlyExcelReport(userId, month, year, category);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Monthly expense report.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelData);
    }

    @Operation(summary = "Method to get all the expense details in a particular year")
    @GetMapping("/getExpenses/{year}/{category}/{deleteStatus}")
    public ResponseEntity<List<ExpenseModel>> getAllExpensesByYearAndCategory(@RequestHeader("Authorization") String authHeader,
                                                                   @PathVariable("year") int year,
                                                                   @PathVariable("category") String category,
                                                                   @PathVariable("deleteStatus") boolean deleteStatus){
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));

        return ResponseEntity.status(HttpStatus.OK).body(expenseService.getAllExpensesByYearAndCategory(userId, year, category, deleteStatus));
    }

    @Operation(summary = "Method to generate the Excel report for the Yearly Expenses of a user")
    @GetMapping("/{year}/{category}/generateYearlyReport")
    public ResponseEntity<byte[]> getYearlyExpenseReport(@RequestHeader("Authorization") String authHeader,
                                                         @PathVariable("year") int year,
                                                         @PathVariable("category") String category) throws IOException {
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));

        byte[] excelData = expenseService.generateYearlyExcelReport(userId, year, category);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Yearly expense report.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelData);
    }

    @Operation(summary = "Method to get the total expense amount in a particular month and in a particular year")
    @GetMapping("/totalExpense/{month}/{year}")
    public BigDecimal getTotalExpenseByMonthAndYear(@RequestHeader("Authorization") String authHeader,
                                                    @PathVariable("month") int month,
                                                    @PathVariable("year") int year){
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));

        return expenseService.getTotalExpenseInMonthAndYear(userId, month, year);
    }

    @Operation(summary = "Method to get the list of total expense amount of all months in a particular year")
    @GetMapping("/monthlyTotalExpensesList/{year}")
    public List<BigDecimal> getMonthlyTotals(@RequestHeader("Authorization") String authHeader,
                                             @PathVariable("year") int year) {
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));

        return expenseService.getMonthlyExpenses(userId, year);
    }

    @Operation(summary = "Method to get the list of total saving amount of all months in a particular year")
    @GetMapping("/monthlySavingsInYear/{year}")
    public List<BigDecimal> getMonthlySavingsList(@RequestHeader("Authorization") String authHeader,
                                                  @PathVariable("year") int year) {
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));

        return expenseService.getMonthlySavingsList(userId, year);
    }

    @Operation(summary = "Method to get the total expense amount up to previous month (excludes current month)")
    @GetMapping("/totalExpensesUpToPreviousMonth/{month}/{year}")
    public BigDecimal getTotalExpensesUpToPreviousMonth(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("month") int month,
            @PathVariable("year") int year) {
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));

        return expenseService.getTotalExpensesUpToPreviousMonth(userId, month, year);
    }

    @Operation(summary = "Method to find the total savings/ remaining amount in a month")
    @GetMapping("/" +
            "/{month}/{year}")
    public BigDecimal getTotalSavingsByMonthAndDate(@RequestHeader("Authorization") String authHeader,
                                                @PathVariable("month") int month,
                                                @PathVariable("year") int year){
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));

        return expenseService.getTotalSavingsByMonthAndDate(userId, month, year);
    }

    @Operation(summary = "Method to get the list of cumulative savings monthly wise in a year")
    @GetMapping("/monthlyCumulativeSavingsInYear/{year}")
    public List<BigDecimal> getCumulativeMonthlySavings(@RequestHeader("Authorization") String authHeader,
                                                        @PathVariable("year") int year){
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));

        return expenseService.getCumulativeMonthlySavings(userId, year);
    }

    @Operation(summary = "Method to update the expense details")
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseModel> updateExpense(@PathVariable("id") Long id,
                                                      @RequestBody ExpenseModel expense) {
        ExpenseModel expenseModel = expenseRepository.findById(id).orElse(null);
        if(expenseModel != null){
            if(expenseModel.getAmount().compareTo(expense.getAmount()) == 0 &&
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
