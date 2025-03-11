package com.finance.income.api;

import com.finance.income.model.IncomeModel;
import com.finance.income.service.IncomeService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/income")
public class IncomeApiController {

    @Autowired
    private IncomeService incomeService;

    @Operation(summary = "Method to save the income details")
    @PostMapping("/{userId}")
    public ResponseEntity<IncomeModel> saveIncome(@RequestBody IncomeModel income,
                                                  @PathVariable("userId") int userId) {
        income.setUserId(userId);
        IncomeModel income1 = incomeService.save(income);
        if (income1 != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(income1); // 201
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(null); // 200
        }
    }

    @Operation(summary = "Method to get all the income details of a user")
    @GetMapping("/{userId}")
    public ResponseEntity<List<IncomeModel>> getAllIncomes(@PathVariable("userId") int userId) {
        List<IncomeModel> list = incomeService.getAllIncomes(userId);
            return ResponseEntity.status(HttpStatus.OK).body(list); // 200
    }

    @Operation(summary = "Method to get all the income details in a particular month and in a particular year")
    @GetMapping("/{userId}/{month}/{year}/{deleteStatus}")
    public ResponseEntity<List<IncomeModel>> getAllIncomesByDate(@PathVariable("userId") int userId,
                                                                 @PathVariable("month") int month,
                                                                 @PathVariable("year") int year,
                                                                 @PathVariable("deleteStatus") boolean deleteStatus){
        List<IncomeModel> incomesList = incomeService.getAllIncomesByDate(userId, month, year, deleteStatus);
        return ResponseEntity.ok(incomesList);
    }

    @Operation(summary = "Method to get all the income details in a particular year")
    @GetMapping("/{userId}/{year}/{deleteStatus}")
    public ResponseEntity<List<IncomeModel>> getAllIncomesByYear(@PathVariable("userId") int userId,
                                                                 @PathVariable("year") int year,
                                                                 @PathVariable("deleteStatus") boolean deleteStatus){
        List<IncomeModel> incomesList = incomeService.getAllIncomesByYear(userId, year, deleteStatus);
        return ResponseEntity.ok(incomesList); // 200
    }

    @Operation(summary = "Method to get the total income amount in a particular month and in a particular year")
    @GetMapping("/{userId}/totalIncome/{month}/{year}")
    public Double getTotalIncomeByMonthAndYear(@PathVariable("userId") int userId, @PathVariable("month") int month, @PathVariable("year") int year){
        return incomeService.getTotalIncomeInMonthAndYear(userId, month, year);
    }

    @Operation(summary = "Method to get the list of total income amount of all months in a particular year")
    @GetMapping("/{userId}/monthlyTotalIncomesList/{year}")
    public List<Double> getMonthlyTotals(@PathVariable("userId") int userId, @PathVariable("year") int year) {
        return incomeService.getMonthlyIncomes(userId, year);
    }

    @Operation(summary = "Method to get the total savings/remaining amount up to previous month (excludes current month)")
    @GetMapping("/{userId}/totalRemainingIncomeUpToPreviousMonth/{month}/{year}")
    public Double getRemainingIncomeUpToPreviousMonthByMonthAndYear(
            @PathVariable("userId") int userId,
            @PathVariable("month") int month,
            @PathVariable("year") int year) {

        return incomeService.getRemainingIncomeUpToPreviousMonthByMonthAndYear(userId, month, year);
    }

    @Operation(summary = "Method to update the income details")
    @PutMapping("/{id}")
    public ResponseEntity<IncomeModel> updateIncome(@PathVariable("id") int id, @RequestBody IncomeModel income) {
        IncomeModel updatedIncome = incomeService.updateBySource(id, income);

        if(updatedIncome!=null){
            return ResponseEntity.status(HttpStatus.CREATED).body(updatedIncome); // 201
        }
        else{
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null); // 409
        }
    }

    @Operation(summary = "Method to delete the particular income. Here which is typically soft delete only")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncomeById(@PathVariable("id") int id) {
        boolean isDeleted = incomeService.deleteIncomeById(id);
        if (isDeleted) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // 204: No Content
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404: Not Found
        }
    }

}
