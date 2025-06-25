package com.moneyfi.income.controller;

import com.moneyfi.income.config.JwtService;
import com.moneyfi.income.service.dto.request.AccountStatementInputDto;
import com.moneyfi.income.service.dto.response.AccountStatementDto;
import com.moneyfi.income.service.dto.response.IncomeDeletedDto;
import com.moneyfi.income.model.IncomeModel;
import com.moneyfi.income.service.IncomeService;
import com.moneyfi.income.service.dto.response.IncomeDetailsDto;
import com.moneyfi.income.service.dto.response.OverviewPageDetailsDto;
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
@RequestMapping("/api/v1/income")
public class IncomeApiController {

    private final IncomeService incomeService;
    private final JwtService jwtService;

    public IncomeApiController(IncomeService incomeService,
                               JwtService jwtService){
        this.incomeService = incomeService;
        this.jwtService = jwtService;
    }


    @Operation(summary = "Method to save the income details")
    @PostMapping("/saveIncome")
    public ResponseEntity<IncomeModel> saveIncome(@RequestBody IncomeModel income,
                                                  @RequestHeader("Authorization") String authHeader) {
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        income.setUserId(userId);
        IncomeModel income1 = incomeService.save(income);
        if (income1 != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(income1); // 201
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(null); // 200
        }
    }

    @Operation(summary = "Method to get all the income details of a user")
    @GetMapping("/getIncomeDetails")
    public ResponseEntity<List<IncomeModel>> getAllIncomes(@RequestHeader("Authorization") String authHeader) {
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        List<IncomeModel> list = incomeService.getAllIncomes(userId);
        return ResponseEntity.status(HttpStatus.OK).body(list); // 200
    }

    @Operation(summary = "Method to get all the income details in a particular month and in a particular year")
    @GetMapping("/getIncomeDetails/{month}/{year}/{category}/{deleteStatus}")
    public ResponseEntity<List<IncomeDetailsDto>> getAllIncomesByMonthYearAndCategory(@RequestHeader("Authorization") String authHeader,
                                                                                      @PathVariable("month") int month,
                                                                                      @PathVariable("year") int year,
                                                                                      @PathVariable("category") String category,
                                                                                      @PathVariable("deleteStatus") boolean deleteStatus){
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        List<IncomeDetailsDto> incomesList = incomeService.getAllIncomesByMonthYearAndCategory(userId, month, year, category, deleteStatus);
        return ResponseEntity.ok(incomesList);
    }

    @Operation(summary = "Method to generate the Excel report for the Monthly incomes of a user")
    @GetMapping("/{month}/{year}/{category}/generateMonthlyReport")
    public ResponseEntity<byte[]> getMonthlyIncomeReport(@RequestHeader("Authorization") String authHeader,
                                                         @PathVariable("month") int month,
                                                         @PathVariable("year") int year,
                                                         @PathVariable("category") String category) {
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        byte[] excelData = incomeService.generateMonthlyExcelReport(userId, month, year, category);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Monthly income report.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelData);
    }

    @Operation(summary = "Method to get the monthly deleted incomes")
    @GetMapping("/getDeletedIncomeDetails/{month}/{year}")
    public ResponseEntity<List<IncomeDeletedDto>> getDeletedIncomesInAMonth(@RequestHeader("Authorization") String authHeader,
                                                                            @PathVariable("month") int month,
                                                                            @PathVariable("year") int year) {
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        List<IncomeDeletedDto> incomesList = incomeService.getDeletedIncomesInAMonth(userId, month, year);
        return ResponseEntity.ok(incomesList);
    }

    @Operation(summary = "Method to get all the income details in a particular year")
    @GetMapping("/getIncomeDetails/{year}/{category}/{deleteStatus}")
    public ResponseEntity<List<IncomeDetailsDto>> getAllIncomesByYear(@RequestHeader("Authorization") String authHeader,
                                                                 @PathVariable("year") int year,
                                                                 @PathVariable("category") String category,
                                                                 @PathVariable("deleteStatus") boolean deleteStatus){
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        List<IncomeDetailsDto> incomesList = incomeService.getAllIncomesByYear(userId, year, category, deleteStatus);
        return ResponseEntity.ok(incomesList); // 200
    }

    @Operation(summary = "Method to generate the Excel report for the Yearly incomes of a user")
    @GetMapping("/{year}/{category}/generateYearlyReport")
    public ResponseEntity<byte[]> getYearlyIncomeReport(@RequestHeader("Authorization") String authHeader,
                                                        @PathVariable("year") int year,
                                                        @PathVariable("category") String category) {

        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        byte[] excelData = incomeService.generateYearlyExcelReport(userId, year, category);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Yearly income report.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelData);
    }

    @Operation(summary = "Method to get the total income amount in a particular month and in a particular year")
    @GetMapping("/totalIncome/{month}/{year}")
    public BigDecimal getTotalIncomeByMonthAndYear(@RequestHeader("Authorization") String authHeader,
                                                   @PathVariable("month") int month,
                                                   @PathVariable("year") int year){
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return incomeService.getTotalIncomeInMonthAndYear(userId, month, year);
    }

    @Operation(summary = "Method to get the list of total income amount of all months in a particular year")
    @GetMapping("/monthlyTotalIncomesList/{year}")
    public List<BigDecimal> getMonthlyTotals(@RequestHeader("Authorization") String authHeader,
                                             @PathVariable("year") int year) {
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return incomeService.getMonthlyIncomes(userId, year);
    }

    @Operation(summary = "Method to check the particular income can be editable")
    @PostMapping("/incomeUpdateCheck")
    public boolean incomeUpdateCheckFunction(@RequestHeader("Authorization") String authHeader,
                                             @RequestBody IncomeModel incomeModel){
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return incomeService.incomeUpdateCheckFunction(incomeModel, userId);
    }

    @Operation(summary = "Method to check the particular income can be deleted")
    @PostMapping("/incomeDeleteCheck")
    public boolean incomeDeleteCheckFunction(@RequestHeader("Authorization") String authHeader,
                                             @RequestBody IncomeModel incomeModel){
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        incomeModel.setUserId(userId);
        return incomeService.incomeDeleteCheckFunction(incomeModel);
    }

    @Operation(summary = "Method to revert the income back from deleted to normal list")
    @GetMapping("/incomeRevert/{id}")
    public boolean incomeRevertFunction(@RequestHeader("Authorization") String authHeader,
                                        @PathVariable("id") Long incomeId){
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return incomeService.incomeRevertFunction(incomeId, userId);
    }

    @Operation(summary = "Method to find the available balance for a user")
    @GetMapping("/availableBalance")
    public BigDecimal getAvailableBalanceOfUser(@RequestHeader("Authorization") String authHeader){
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return incomeService.getAvailableBalanceOfUser(userId);
    }

    @Operation(summary = "Api to get overall transactions in the selected period")
    @PostMapping("/account-statement")
    public List<AccountStatementDto> getAccountStatementOfUser(@RequestHeader("Authorization") String authHeader,
                                                               @RequestBody AccountStatementInputDto inputDto){
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return incomeService.getAccountStatementOfUser(userId, inputDto);
    }

    @Operation(summary = "Api to generate pdf for the account statement")
    @GetMapping("/account-statement/report")
    public byte[] generatePdfForAccountStatement(@RequestHeader("Authorization") String authHeader,
                                                 @RequestBody AccountStatementInputDto inputDto) throws IOException {
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return incomeService.generatePdfForAccountStatement(userId, inputDto);
    }

    @Operation(summary = "Api to send account statement of a user as email")
    @GetMapping("/account-statement-report/email")
    public ResponseEntity<String> sendAccountStatementEmailToUser(@RequestHeader("Authorization") String authHeader,
                                                                  @RequestBody AccountStatementInputDto inputDto) throws IOException {
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return incomeService.sendAccountStatementEmailToUser(userId, inputDto, authHeader);
    }

    @Operation(summary = "Api to get the overview page tile details")
    @GetMapping("/overview-details/{month}/{year}")
    public OverviewPageDetailsDto getOverviewPageTileDetails(@RequestHeader("Authorization") String authHeader,
                                                             @PathVariable("month") int month,
                                                             @PathVariable("year") int year){
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return incomeService.getOverviewPageTileDetails(userId, month, year);
    }

    @Operation(summary = "Method to update the income details")
    @PutMapping("/{id}")
    public ResponseEntity<IncomeDetailsDto> updateIncome(@RequestHeader("Authorization") String authHeader,
                                                    @PathVariable("id") Long id,
                                                    @RequestBody IncomeModel income) {

        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return incomeService.updateBySource(id, userId, income);
    }

    @Operation(summary = "Method to delete the particular income. Here which is typically soft delete only")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncomeById(@RequestHeader("Authorization") String authHeader,
                                                 @PathVariable("id") Long id) {
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        boolean isDeleted = incomeService.deleteIncomeById(id, userId);
        if (isDeleted) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // 204: No Content
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404: Not Found
        }
    }
}
