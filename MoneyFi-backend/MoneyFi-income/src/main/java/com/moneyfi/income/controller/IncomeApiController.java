package com.moneyfi.income.controller;

import com.moneyfi.income.model.IncomeModel;
import com.moneyfi.income.repository.IncomeRepository;
import com.moneyfi.income.service.IncomeService;
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
@RequestMapping("/api/income")
public class IncomeApiController {

    private final IncomeService incomeService;
    private final IncomeRepository incomeRepository;

    public IncomeApiController(IncomeService incomeService,
                               IncomeRepository incomeRepository){
        this.incomeService = incomeService;
        this.incomeRepository = incomeRepository;
    }

//    @GetMapping("/test")
//    public Object testFunction(Authentication authentication) {
//        if (authentication == null) {
//            return "No authentication present.";
//        }
//        return authentication.getPrincipal();
//    }


    @Operation(summary = "Method to save the income details")
    @PostMapping("/{userId}")
    public ResponseEntity<IncomeModel> saveIncome(@RequestBody IncomeModel income,
                                                  @PathVariable("userId") Long userId) {
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
    public ResponseEntity<List<IncomeModel>> getAllIncomes(@PathVariable("userId") Long userId) {
        List<IncomeModel> list = incomeService.getAllIncomes(userId);
            return ResponseEntity.status(HttpStatus.OK).body(list); // 200
    }

    @Operation(summary = "Method to get all the income details in a particular month and in a particular year")
    @GetMapping("/{userId}/{month}/{year}/{category}/{deleteStatus}")
    public ResponseEntity<List<IncomeModel>> getAllIncomesByMonthYearAndCategory(@PathVariable("userId") Long userId,
                                                                 @PathVariable("month") int month,
                                                                 @PathVariable("year") int year,
                                                                 @PathVariable("category") String category,
                                                                 @PathVariable("deleteStatus") boolean deleteStatus){
        List<IncomeModel> incomesList = incomeService.getAllIncomesByMonthYearAndCategory(userId, month, year, category, deleteStatus);
        return ResponseEntity.ok(incomesList);
    }

    @Operation(summary = "Method to generate the Excel report for the Monthly incomes of a user")
    @GetMapping("/{userId}/{month}/{year}/{category}/generateMonthlyReport")
    public ResponseEntity<byte[]> getMonthlyIncomeReport(@PathVariable("userId") Long userId,
                                                         @PathVariable("month") int month,
                                                         @PathVariable("year") int year,
                                                         @PathVariable("category") String category) throws IOException {

        byte[] excelData = incomeService.generateMonthlyExcelReport(userId, month, year, category);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Monthly income report.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelData);
    }

    @Operation(summary = "Method to get all the income details in a particular year")
    @GetMapping("/{userId}/{year}/{category}/{deleteStatus}")
    public ResponseEntity<List<IncomeModel>> getAllIncomesByYear(@PathVariable("userId") Long userId,
                                                                 @PathVariable("year") int year,
                                                                 @PathVariable("category") String category,
                                                                 @PathVariable("deleteStatus") boolean deleteStatus){
        List<IncomeModel> incomesList = incomeService.getAllIncomesByYear(userId, year, category, deleteStatus);
        return ResponseEntity.ok(incomesList); // 200
    }

    @Operation(summary = "Method to generate the Excel report for the Yearly incomes of a user")
    @GetMapping("/{userId}/{year}/{category}/generateYearlyReport")
    public ResponseEntity<byte[]> getYearlyIncomeReport(@PathVariable("userId") Long userId,
                                                        @PathVariable("year") int year,
                                                        @PathVariable("category") String category) throws IOException {

        byte[] excelData = incomeService.generateYearlyExcelReport(userId, year, category);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Yearly income report.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelData);
    }

    @Operation(summary = "Method to get the total income amount in a particular month and in a particular year")
    @GetMapping("/{userId}/totalIncome/{month}/{year}")
    public BigDecimal getTotalIncomeByMonthAndYear(@PathVariable("userId") Long userId,
                                                   @PathVariable("month") int month,
                                                   @PathVariable("year") int year){
        return incomeService.getTotalIncomeInMonthAndYear(userId, month, year);
    }

    @Operation(summary = "Method to get the list of total income amount of all months in a particular year")
    @GetMapping("/{userId}/monthlyTotalIncomesList/{year}")
    public List<BigDecimal> getMonthlyTotals(@PathVariable("userId") Long userId,
                                             @PathVariable("year") int year) {
        return incomeService.getMonthlyIncomes(userId, year);
    }

    @Operation(summary = "Method to get the total savings/remaining amount up to previous month (excludes current month)")
    @GetMapping("/{userId}/totalRemainingIncomeUpToPreviousMonth/{month}/{year}")
    public BigDecimal getRemainingIncomeUpToPreviousMonthByMonthAndYear(
            @PathVariable("userId") Long userId,
            @PathVariable("month") int month,
            @PathVariable("year") int year) {

        return incomeService.getRemainingIncomeUpToPreviousMonthByMonthAndYear(userId, month, year);
    }

    @Operation(summary = "Method to check the particular income can be editable")
    @PostMapping("/{userId}/incomeUpdateCheck")
    public boolean incomeUpdateCheckFunction(@RequestBody IncomeModel incomeModel){
        return incomeService.incomeUpdateCheckFunction(incomeModel);
    }

    @Operation(summary = "Method to check the particular income can be deleted")
    @PostMapping("/{userId}/incomeDeleteCheck")
    public boolean incomeDeleteCheckFunction(@RequestBody IncomeModel incomeModel){
        return incomeService.incomeDeleteCheckFunction(incomeModel);
    }

    @Operation(summary = "Method to update the income details")
    @PutMapping("/{id}")
    public ResponseEntity<IncomeModel> updateIncome(@PathVariable("id") Long id,
                                                    @RequestBody IncomeModel income) {

        IncomeModel incomeModel = incomeRepository.findById(id).orElse(null);
        if(incomeModel != null){
            if(incomeModel.getAmount().compareTo(income.getAmount()) == 0 &&
                    incomeModel.getSource().equals(income.getSource()) &&
                    incomeModel.getCategory().equals(income.getCategory()) &&
                    incomeModel.getDate().equals(income.getDate()) &&
                    incomeModel.isRecurring() == income.isRecurring()){
                return ResponseEntity.noContent().build(); // HTTP 204

            }
        }

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
    public ResponseEntity<Void> deleteIncomeById(@PathVariable("id") Long id) {
        boolean isDeleted = incomeService.deleteIncomeById(id);
        if (isDeleted) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // 204: No Content
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404: Not Found
        }
    }

}
