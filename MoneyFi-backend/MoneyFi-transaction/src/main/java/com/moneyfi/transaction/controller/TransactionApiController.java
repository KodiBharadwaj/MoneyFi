package com.moneyfi.transaction.controller;

import com.moneyfi.transaction.config.JwtService;
import com.moneyfi.transaction.service.income.dto.request.AccountStatementRequestDto;
import com.moneyfi.transaction.service.income.dto.response.AccountStatementResponseDto;
import com.moneyfi.transaction.service.income.dto.response.OverviewPageDetailsDto;
import com.moneyfi.transaction.service.transaction.TransactionService;
import com.moneyfi.transaction.service.transaction.dto.request.ParsedTransaction;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transaction")
public class TransactionApiController {

    private final JwtService jwtService;
    private final TransactionService transactionService;

    public TransactionApiController(TransactionService transactionService,
                                    JwtService jwtService){
        this.transactionService = transactionService;
        this.jwtService = jwtService;
    }

    @Operation(summary = "Api to get the overview page tile details")
    @GetMapping("/overview-details/{month}/{year}")
    public OverviewPageDetailsDto getOverviewPageTileDetails(@RequestHeader("Authorization") String authHeader,
                                                             @PathVariable("month") int month,
                                                             @PathVariable("year") int year){
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return transactionService.getOverviewPageTileDetails(userId, month, year);
    }

    @Operation(summary = "Api to get overall transactions in the selected period")
    @PostMapping("/account-statement")
    public List<AccountStatementResponseDto> getAccountStatementOfUser(@RequestHeader("Authorization") String authHeader,
                                                                       @RequestBody AccountStatementRequestDto inputDto){
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        return transactionService.getAccountStatementOfUser(userId, inputDto);
    }

    @Operation(summary = "Api to generate pdf for the account statement")
    @PostMapping("/account-statement/report")
    public ResponseEntity<byte[]> generatePdfForAccountStatement(@RequestHeader("Authorization") String authHeader,
                                                                 @RequestBody AccountStatementRequestDto inputDto) throws IOException {
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        byte[] pdfBytes = transactionService.generatePdfForAccountStatement(userId, inputDto);
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
        return transactionService.sendAccountStatementEmailToUser(userId, inputDto, authHeader);
    }

    @Operation(summary = "Api to save income-expense transactions from moneyfi gmail sync")
    @PostMapping("/gmail-sync/bulk-save")
    public ResponseEntity<Void> saveBulk(@RequestHeader("Authorization") String authHeader,
                                         @RequestBody List<ParsedTransaction> transactions) {
        Long userId = jwtService.extractUserIdFromToken(authHeader.substring(7));
        transactionService.addGmailSyncTransactions(userId, transactions);
        return ResponseEntity.ok().build();
    }
}
