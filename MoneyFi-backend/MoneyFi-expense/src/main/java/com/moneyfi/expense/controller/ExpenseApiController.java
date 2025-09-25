package com.moneyfi.expense.controller;

import com.moneyfi.expense.config.JwtService;
import com.moneyfi.expense.model.ExpenseModel;
import com.moneyfi.expense.service.ExpenseService;
import com.moneyfi.expense.service.dto.response.ExpenseDetailsDto;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/expense")
public class ExpenseApiController {

    private final ExpenseService expenseService;
    private final JwtService jwtService;

    public ExpenseApiController(ExpenseService expenseService,
                                JwtService jwtService){
        this.expenseService = expenseService;
        this.jwtService = jwtService;
    }

    @Operation(summary = "Method to add the expense transaction")
    @PostMapping("/saveExpense")
    public ResponseEntity<ExpenseModel> saveExpense(@RequestHeader("Authorization") String authHeader,
                                                    @RequestBody ExpenseModel expense) {
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
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
    public ResponseEntity<List<ExpenseDetailsDto>> getAllExpensesByMonthYearAndCategory(@RequestHeader("Authorization") String authHeader,
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
                                                          @PathVariable("year") int year) {

        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        byte[] excelData = expenseService.generateMonthlyExcelReport(userId, month, year, category);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Monthly expense report.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelData);
    }

    @Operation(summary = "Method to get all the expense details in a particular year")
    @GetMapping("/getExpenses/{year}/{category}/{deleteStatus}")
    public ResponseEntity<List<ExpenseDetailsDto>> getAllExpensesByYearAndCategory(@RequestHeader("Authorization") String authHeader,
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
                                                         @PathVariable("category") String category) {
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


    @Operation(summary = "Method to find the total savings/remaining amount in a month")
    @GetMapping("/{month}/{year}")
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

    @Operation(summary = "Api to get the total expense in a specified period")
    @GetMapping("/total-expenses/specified-range")
    public BigDecimal getTotalExpensesInSpecifiedRange(@RequestHeader("Authorization") String authHeader,
                                                       @RequestParam LocalDate fromDate,
                                                       @RequestParam LocalDate toDate){
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return expenseService.getTotalExpensesInSpecifiedRange(userId, fromDate.atStartOfDay(), toDate.atTime(LocalTime.MAX));
    }

    @Operation(summary = "Method to update the expense details")
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseDetailsDto> updateExpense(@RequestHeader("Authorization") String authHeader,
                                                      @PathVariable("id") Long id,
                                                      @RequestBody ExpenseModel expense) {
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return expenseService.updateBySource(id, userId, expense);
    }

    @Operation(summary = "Method to delete the particular expense. Here which is typically soft delete")
    @DeleteMapping
    public ResponseEntity<Void> deleteExpenseById(@RequestBody List<Long> ids) {
        boolean isDeleted = expenseService.deleteExpenseById(ids);
        if (isDeleted) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // 204: No Content
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404: Not Found
        }
    }


}
