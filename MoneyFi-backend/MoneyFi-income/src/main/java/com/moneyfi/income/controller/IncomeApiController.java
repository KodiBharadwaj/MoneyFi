package com.moneyfi.income.controller;

import com.moneyfi.income.config.JwtService;
import com.moneyfi.income.service.dto.request.AccountStatementRequestDto;
import com.moneyfi.income.service.dto.request.IncomeSaveRequest;
import com.moneyfi.income.service.dto.request.IncomeUpdateRequest;
import com.moneyfi.income.service.dto.response.AccountStatementResponseDto;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/income-service/user")
public class IncomeApiController {

    private final IncomeService incomeService;
    private final JwtService jwtService;

    public IncomeApiController(IncomeService incomeService,
                               JwtService jwtService){
        this.incomeService = incomeService;
        this.jwtService = jwtService;
    }


    @Operation(summary = "Api to save income details of user")
    @PostMapping("/save")
    public void saveIncome(@RequestHeader("Authorization") String authHeader,
                           @RequestBody IncomeSaveRequest incomeSaveRequest) {
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        incomeService.saveIncome(incomeSaveRequest, userId);
    }

    @Operation(summary = "Api to get all the income details of a user")
    @GetMapping("/get")
    public ResponseEntity<List<IncomeModel>> getAllIncomes(@RequestHeader("Authorization") String authHeader) {
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return ResponseEntity.ok(incomeService.getAllIncomes(userId));
    }

    @Operation(summary = "Api to get all the income details in a particular month and year")
    @GetMapping("/{month}/{year}/{category}/{deleteStatus}/incomes-list/get")
    public ResponseEntity<List<IncomeDetailsDto>> getAllIncomesByMonthYearAndCategory(@RequestHeader("Authorization") String authHeader,
                                                                                      @PathVariable("month") int month,
                                                                                      @PathVariable("year") int year,
                                                                                      @PathVariable("category") String category,
                                                                                      @PathVariable("deleteStatus") boolean deleteStatus){
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return ResponseEntity.ok(incomeService.getAllIncomesByMonthYearAndCategory(userId, month, year, category, deleteStatus));
    }

    @Operation(summary = "Api to generate Excel report for monthly incomes of a user")
    @GetMapping("/{month}/{year}/{category}/incomes-list/report")
    public ResponseEntity<byte[]> getMonthlyIncomeReport(@RequestHeader("Authorization") String authHeader,
                                                         @PathVariable("month") int month,
                                                         @PathVariable("year") int year,
                                                         @PathVariable("category") String category) {
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Monthly income report.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(incomeService.generateMonthlyExcelReport(userId, month, year, category));
    }

    @Operation(summary = "Api to get monthly deleted incomes")
    @GetMapping("/{month}/{year}/deleted-incomes-list/get")
    public ResponseEntity<List<IncomeDeletedDto>> getDeletedIncomesInAMonth(@RequestHeader("Authorization") String authHeader,
                                                                            @PathVariable("month") int month,
                                                                            @PathVariable("year") int year) {
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return ResponseEntity.ok(incomeService.getDeletedIncomesInAMonth(userId, month, year));
    }

    @Operation(summary = "Api to get all income details in a particular year")
    @GetMapping("/{year}/{category}/{deleteStatus}/incomes-list/get")
    public ResponseEntity<List<IncomeDetailsDto>> getAllIncomesByYear(@RequestHeader("Authorization") String authHeader,
                                                                      @PathVariable("year") int year,
                                                                      @PathVariable("category") String category,
                                                                      @PathVariable("deleteStatus") boolean deleteStatus){
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return ResponseEntity.ok(incomeService.getAllIncomesByYear(userId, year, category, deleteStatus));
    }

    @Operation(summary = "Api to generate Excel report for Yearly incomes of a user")
    @GetMapping("/{year}/{category}/incomes-list/report")
    public ResponseEntity<byte[]> getYearlyIncomeReport(@RequestHeader("Authorization") String authHeader,
                                                        @PathVariable("year") int year,
                                                        @PathVariable("category") String category) {
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Yearly income report.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(incomeService.generateYearlyExcelReport(userId, year, category));
    }

    @Operation(summary = "Api to get the total income amount in a particular month and in a particular year")
    @GetMapping("/totalIncome/{month}/{year}")
    public BigDecimal getTotalIncomeByMonthAndYear(@RequestHeader("Authorization") String authHeader,
                                                   @PathVariable("month") int month,
                                                   @PathVariable("year") int year){
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return incomeService.getTotalIncomeInMonthAndYear(userId, month, year);
    }

    @Operation(summary = "Api to get the list of total income amount of all months in a particular year")
    @GetMapping("/monthlyTotalIncomesList/{year}")
    public List<BigDecimal> getMonthlyTotals(@RequestHeader("Authorization") String authHeader,
                                             @PathVariable("year") int year) {
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return incomeService.getMonthlyIncomes(userId, year);
    }

    @Operation(summary = "Api to check a particular income can be editable")
    @PostMapping("/incomeUpdateCheck")
    public boolean incomeUpdateCheckFunction(@RequestHeader("Authorization") String authHeader,
                                             @RequestBody IncomeModel incomeModel){
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return incomeService.incomeUpdateCheckFunction(incomeModel, userId);
    }

    @Operation(summary = "Api to check the particular income can be deleted")
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
    public List<AccountStatementResponseDto> getAccountStatementOfUser(@RequestHeader("Authorization") String authHeader,
                                                                       @RequestBody AccountStatementRequestDto inputDto){
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return incomeService.getAccountStatementOfUser(userId, inputDto);
    }

    @Operation(summary = "Api to generate pdf for the account statement")
    @PostMapping("/account-statement/report")
    public ResponseEntity<byte[]> generatePdfForAccountStatement(@RequestHeader("Authorization") String authHeader,
                                                 @RequestBody AccountStatementRequestDto inputDto) throws IOException {
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        byte[] pdfBytes = incomeService.generatePdfForAccountStatement(userId, inputDto);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=account-statement.pdf")
            .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
            .body(pdfBytes);
    }

    @Operation(summary = "Api to send account statement of a user as email")
    @PostMapping("/account-statement-report/email")
    public ResponseEntity<String> sendAccountStatementEmailToUser(@RequestHeader("Authorization") String authHeader,
                                                                  @RequestBody AccountStatementRequestDto inputDto) {
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

    @Operation(summary = "Api to get the total income in a specified date range")
    @GetMapping("/total-income/specified-range")
    public List<Object[]> getTotalIncomeInSpecifiedRange(@RequestHeader("Authorization") String authHeader,
                                                         @RequestParam LocalDate fromDate,
                                                         @RequestParam LocalDate toDate){
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return incomeService.getTotalIncomeInSpecifiedRange(userId, fromDate.atStartOfDay(), toDate.atTime(LocalTime.MAX));
    }

    @Operation(summary = "Api to update the income details")
    @PutMapping("/update/{id}")
    public void updateIncome(@RequestHeader("Authorization") String authHeader,
                             @PathVariable("id") Long id,
                             @RequestBody IncomeUpdateRequest IncomeUpdateRequest) {

        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        incomeService.updateBySource(id, userId, IncomeUpdateRequest);
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
