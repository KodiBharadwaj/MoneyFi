package com.moneyfi.transaction.service.transaction.impl;

import com.moneyfi.transaction.exceptions.ResourceNotFoundException;
import com.moneyfi.transaction.exceptions.ScenarioNotPossibleException;
import com.moneyfi.transaction.model.expense.ExpenseModel;
import com.moneyfi.transaction.model.income.IncomeModel;
import com.moneyfi.transaction.repository.expense.ExpenseRepository;
import com.moneyfi.transaction.repository.income.IncomeRepository;
import com.moneyfi.transaction.repository.transaction.TransactionRepository;
import com.moneyfi.transaction.service.expense.dto.response.ExpenseDetailsDto;
import com.moneyfi.transaction.service.income.dto.request.AccountStatementRequestDto;
import com.moneyfi.transaction.service.income.dto.request.IncomeSaveRequest;
import com.moneyfi.transaction.service.income.dto.response.AccountStatementResponseDto;
import com.moneyfi.transaction.service.income.dto.response.IncomeDetailsDto;
import com.moneyfi.transaction.service.income.dto.response.OverviewPageDetailsDto;
import com.moneyfi.transaction.service.income.dto.response.UserDetailsForStatementDto;
import com.moneyfi.transaction.service.transaction.TransactionService;
import com.moneyfi.transaction.service.transaction.dto.request.ParsedTransaction;
import com.moneyfi.transaction.service.transaction.dto.response.GmailSyncTransactionsResponse;
import com.moneyfi.transaction.utils.enums.CreditOrDebit;
import com.moneyfi.transaction.utils.GeneratePdfTemplate;
import com.moneyfi.transaction.utils.StringConstants;
import com.moneyfi.transaction.utils.enums.EntryModeEnum;
import com.moneyfi.transaction.utils.enums.TransactionServiceType;
import com.moneyfi.transaction.validator.IncomeValidator;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.moneyfi.transaction.utils.StringConstants.*;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final TransactionRepository transactionRepository;
    private final GeneratePdfTemplate generatePdfTemplate;
    private final RestTemplate restTemplate;

    public TransactionServiceImpl(IncomeRepository incomeRepository,
                                  ExpenseRepository expenseRepository,
                                  TransactionRepository transactionRepository,
                                  GeneratePdfTemplate generatePdfTemplate,
                                  RestTemplate restTemplate){
        this.incomeRepository = incomeRepository;
        this.expenseRepository = expenseRepository;
        this.transactionRepository = transactionRepository;
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

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void addGmailSyncTransactions(Long userId, LocalDate syncDate, List<ParsedTransaction> transactions) {
        List<IncomeModel> incomesToBeSaved = new ArrayList<>();
        List<ExpenseModel> expensesToBeSaved = new ArrayList<>();

        for (ParsedTransaction transaction : transactions) {
            if (transaction.getTransactionType().equalsIgnoreCase(CreditOrDebit.CREDIT.name())) {
                IncomeValidator.validateIncomeSaveRequest(new IncomeSaveRequest(transaction.getAmount(), transaction.getDescription(), transaction.getTransactionDate().toString(), transaction.getCategoryId(), false, transaction.getDescription()), userId);
                List<Integer> incomeCategoryIds = transactionRepository.getCategoryIdsBasedOnTransactionType(TransactionServiceType.INCOME.name());
                if (!incomeCategoryIds.contains(transaction.getCategoryId())) {
                    throw new ScenarioNotPossibleException(CATEGORY_NOT_ALIGN_MESSAGE);
                }
                incomesToBeSaved.add(getSaveIncomeModel(transaction, syncDate, userId));
            } else if (transaction.getTransactionType().equalsIgnoreCase(CreditOrDebit.DEBIT.name())) {
                List<Integer> expenseCategoryIds = transactionRepository.getCategoryIdsBasedOnTransactionType(TransactionServiceType.EXPENSE.name());
                if (!expenseCategoryIds.contains(transaction.getCategoryId())) {
                    throw new ScenarioNotPossibleException(CATEGORY_NOT_ALIGN_MESSAGE);
                }
                if (ObjectUtils.isEmpty(userId)) {
                    throw new ScenarioNotPossibleException(USER_ID_EMPTY);
                }
                expensesToBeSaved.add(getSaveExpenseModel(transaction, syncDate, userId));
            } else {
                throw new ScenarioNotPossibleException(INVALID_INPUT);
            }
        }
        incomeRepository.saveAll(incomesToBeSaved);
        expenseRepository.saveAll(expensesToBeSaved);
        transactionRepository.updateGmailProcessedAsVerified(transactions.stream().map(ParsedTransaction::getGmailProcessedId).toList());
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public GmailSyncTransactionsResponse getGmailSyncAddedTransactions(Long userId, LocalDate date) {
        return new GmailSyncTransactionsResponse(
                incomeRepository.getGmailSyncAddedIncomes(userId, date)
                        .stream()
                        .map(income -> IncomeDetailsDto.builder()
                                .id(income.getId())
                                .amount(income.getAmount())
                                .source(income.getSource())
                                .date(income.getDate() == null ? null : Date.from(income.getDate().atZone(ZoneId.systemDefault()).toInstant()))
                                .category(incomeRepository.getCategoryNameById(income.getCategoryId()))
                                .recurring(income.isRecurring())
                                .description(income.getDescription())
                                .build()
                        )
                        .toList(),
                expenseRepository.getGmailSyncAddedExpenses(userId, date)
                        .stream()
                        .map(expense -> ExpenseDetailsDto.builder()
                                .id(expense.getId())
                                .amount(expense.getAmount())
                                .description(expense.getDescription())
                                .date(expense.getDate() == null ? null : Date.from(expense.getDate().atZone(ZoneId.systemDefault()).toInstant()))
                                .category(incomeRepository.getCategoryNameById(expense.getCategoryId()))
                                .recurring(expense.isRecurring())
                                .description(expense.getDescription())
                                .build()
                        )
                        .toList()
        );
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

    private IncomeModel getSaveIncomeModel(ParsedTransaction transaction, LocalDate syncDate, Long userId) {
        IncomeModel income = new IncomeModel();
        income.setUserId(userId);
        income.setAmount(transaction.getAmount());
        income.setSource(transaction.getDescription());
        income.setCategoryId(transaction.getCategoryId());
        income.setDescription(transaction.getDescription());
        income.setDate(transaction.getTransactionDate());
        income.setGmailSyncDate(syncDate.atTime(LocalTime.now()));
        income.setRecurring(false);
        income.setEntryMode(EntryModeEnum.GMAIL_SYNC.name());
        return income;
    }

    private ExpenseModel getSaveExpenseModel(ParsedTransaction transaction, LocalDate syncDate, Long userId) {
        ExpenseModel expense = new ExpenseModel();
        expense.setUserId(userId);
        expense.setCategoryId(transaction.getCategoryId());
        expense.setDescription(transaction.getDescription());
        expense.setDate(transaction.getTransactionDate());
        expense.setAmount(transaction.getAmount());
        expense.setRecurring(false);
        expense.setGmailSyncDate(syncDate.atTime(LocalTime.now()));
        expense.setEntryMode(EntryModeEnum.GMAIL_SYNC.name());
        return expense;
    }
}
