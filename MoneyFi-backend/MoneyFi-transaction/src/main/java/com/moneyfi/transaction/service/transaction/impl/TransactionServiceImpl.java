package com.moneyfi.transaction.service.transaction.impl;

import com.moneyfi.transaction.exceptions.ResourceNotFoundException;
import com.moneyfi.transaction.repository.income.IncomeDeletedRepository;
import com.moneyfi.transaction.repository.income.IncomeRepository;
import com.moneyfi.transaction.repository.transaction.TransactionRepository;
import com.moneyfi.transaction.service.income.dto.request.AccountStatementRequestDto;
import com.moneyfi.transaction.service.income.dto.response.AccountStatementResponseDto;
import com.moneyfi.transaction.service.income.dto.response.OverviewPageDetailsDto;
import com.moneyfi.transaction.service.income.dto.response.UserDetailsForStatementDto;
import com.moneyfi.transaction.service.transaction.TransactionService;
import com.moneyfi.transaction.utils.GeneratePdfTemplate;
import com.moneyfi.transaction.utils.StringConstants;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final IncomeRepository incomeRepository;
    private final TransactionRepository transactionRepository;
    private final IncomeDeletedRepository incomeDeletedRepository;
    private final GeneratePdfTemplate generatePdfTemplate;
    private final RestTemplate restTemplate;

    public TransactionServiceImpl(IncomeRepository incomeRepository,
                                  TransactionRepository transactionRepository,
                                  IncomeDeletedRepository incomeDeletedRepository,
                                  GeneratePdfTemplate generatePdfTemplate,
                                  RestTemplate restTemplate){
        this.incomeRepository = incomeRepository;
        this.transactionRepository = transactionRepository;
        this.incomeDeletedRepository = incomeDeletedRepository;
        this.generatePdfTemplate = generatePdfTemplate;
        this.restTemplate = restTemplate;
    }

    @Override
    public OverviewPageDetailsDto getOverviewPageTileDetails(Long userId, int month, int year) {
        return transactionRepository.getOverviewPageTileDetails(userId, month, year);
    }

    @Override
    public List<AccountStatementResponseDto> getAccountStatementOfUser(Long userId, AccountStatementRequestDto inputDto) {
        if(inputDto.getFromDate().isAfter(inputDto.getToDate())){
            throw new IllegalArgumentException("From date should be less than To date");
        }
        AtomicInteger i = new AtomicInteger(1);
        return transactionRepository.getAccountStatementOfUser(userId, inputDto)
                .stream()
                .peek(transaction -> {
                    transaction.setTransactionTime(
                            StringConstants.changeTransactionTimeToTwelveHourFormat(transaction.getTransactionTime())
                    );
                    transaction.setId(i.getAndIncrement());
                }).toList();
    }

    @Override
    public byte[] generatePdfForAccountStatement(Long userId, AccountStatementRequestDto inputDto) throws IOException {
        inputDto.setThreshold(-1); /** to get all the transactions without pagination **/
        UserDetailsForStatementDto userDetails = transactionRepository.getUserDetailsForAccountStatement(userId);
        userDetails.setUsername(makeUsernamePrivate(userDetails.getUsername()));
        return generatePdfTemplate.generatePdf(getAccountStatementOfUser(userId, inputDto), userDetails, inputDto.getFromDate(), inputDto.getToDate(), generateDocumentPasswordForUser(userDetails));
    }

    @Override
    public ResponseEntity<String> sendAccountStatementEmailToUser(Long userId, AccountStatementRequestDto inputDto, String token) {
        try {
            byte[] pdfBytes = generatePdfForAccountStatement(userId, inputDto);
            apiCallToGatewayServiceToSendEmail(pdfBytes, token);
            return ResponseEntity.ok("Email sent successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send email: " + e.getMessage());
        }
    }

    private String makeUsernamePrivate(String username){
        int index = username.indexOf('@');
        return username.substring(0, index/3) + "x".repeat(index - index/3) + username.substring(index);
    }

    private String generateDocumentPasswordForUser(UserDetailsForStatementDto userDetails){
        return userDetails.getName().substring(0,4).toUpperCase() + userDetails.getUsername().substring(0,4).toLowerCase();
    }

    private void apiCallToGatewayServiceToSendEmail(byte[] pdfBytes, String authHeader){
        String token = authHeader.substring(7);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setBearerAuth(token);
        HttpEntity<byte[]> requestEntity = new HttpEntity<>(pdfBytes, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                StringConstants.ACCOUNT_STATEMENT_USER_SERVICE_URL,
                HttpMethod.POST,
                requestEntity,
                Void.class
        );
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new ResourceNotFoundException("Failed to send email: " + response.getStatusCode());
        }
    }
}
