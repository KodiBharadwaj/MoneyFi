package com.moneyfi.wealthcore.service.wealthcore.impl;

import com.moneyfi.wealthcore.exceptions.ResourceNotFoundException;
import com.moneyfi.wealthcore.repository.common.WealthCoreRepository;
import com.moneyfi.wealthcore.service.budget.dto.response.SpendingAnalysisResponseDto;
import com.moneyfi.wealthcore.service.budget.dto.response.UserDetailsForSpendingAnalysisDto;
import com.moneyfi.wealthcore.service.wealthcore.WealthCoreService;
import com.moneyfi.wealthcore.utils.GeneratePdfTemplate;
import com.moneyfi.wealthcore.utils.StringConstants;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.moneyfi.wealthcore.utils.StringConstants.*;
import static com.moneyfi.wealthcore.utils.StringConstants.EMAIL_SENT_FAILURE_MESSAGE;

@Service
public class WealthCoreServiceImpl implements WealthCoreService {

    private final WealthCoreRepository wealthCoreRepository;
    private final RestTemplate restTemplate;
    private final GeneratePdfTemplate generatePdfTemplate;

    public WealthCoreServiceImpl(WealthCoreRepository wealthCoreRepository,
                                 RestTemplate restTemplate,
                                 GeneratePdfTemplate generatePdfTemplate) {
        this.wealthCoreRepository = wealthCoreRepository;
        this.restTemplate = restTemplate;
        this.generatePdfTemplate = generatePdfTemplate;
    }

    @Override
    public SpendingAnalysisResponseDto getUserSpendingAnalysisByBudgetCategories(Long userId, LocalDate fromDate, LocalDate toDate, String authHeader) {
        SpendingAnalysisResponseDto spendingAnalysis = new SpendingAnalysisResponseDto(new HashMap<>(), new HashMap<>(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", authHeader);
        HttpEntity<Object> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<List<Object[]>> incomeResponse = restTemplate.exchange(
                StringConstants.EUREKA_TRANSACTION_SERVICE_URL + "/income/user/total-income/specified-range?fromDate=" + fromDate + "&toDate=" + toDate,
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<List<Object[]>>() {}
        );
        ResponseEntity<List<Object[]>> expenseResponse = restTemplate.exchange(
                StringConstants.EUREKA_TRANSACTION_SERVICE_URL + "/expense/user/total-expenses/specified-range?fromDate=" + fromDate + "&toDate=" + toDate,
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<List<Object[]>>() {}
        );
        ResponseEntity<List<Object[]>> incomeResponseTillToDate = restTemplate.exchange(
                StringConstants.EUREKA_TRANSACTION_SERVICE_URL + "/income/user/total-income/specified-range?fromDate=" + LocalDate.of(1, 1, 1) + "&toDate=" + toDate,
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<List<Object[]>>() {}
        );
        ResponseEntity<List<Object[]>> expenseResponseTillToDate = restTemplate.exchange(
                StringConstants.EUREKA_TRANSACTION_SERVICE_URL + "/expense/user/total-expenses/specified-range?fromDate=" + LocalDate.of(1, 1, 1) + "&toDate=" + toDate,
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<List<Object[]>>() {}
        );

        Map<String, BigDecimal> incomeByCategoryMap = new HashMap<>();
        Objects.requireNonNull(incomeResponse.getBody())
                .forEach(income -> {
                    String category = (String) income[0];
                    BigDecimal amount = BigDecimal.valueOf(((Number) income[1]).doubleValue());
                    incomeByCategoryMap.put(category, amount);
                    spendingAnalysis.setTotalIncome(spendingAnalysis.getTotalIncome().add(amount));
                });
        spendingAnalysis.setIncomeByCategory(incomeByCategoryMap);

        Map<String, BigDecimal> expenseByCategoryMap = new HashMap<>();
        Objects.requireNonNull(expenseResponse.getBody())
                .forEach(expense -> {
                    String category = (String) expense[0];
                    BigDecimal amount = BigDecimal.valueOf(((Number) expense[1]).doubleValue());
                    expenseByCategoryMap.put(category, amount);
                    spendingAnalysis.setTotalExpense(spendingAnalysis.getTotalExpense().add(amount));
                });
        spendingAnalysis.setExpenseByCategory(expenseByCategoryMap);

        BigDecimal totalIncomeTillEndDate = Objects.requireNonNull(incomeResponseTillToDate.getBody())
                .stream()
                .map(income -> BigDecimal.valueOf(((Number) income[1]).doubleValue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpensesTillEndDate = Objects.requireNonNull(expenseResponseTillToDate.getBody())
                .stream()
                .map(expense -> BigDecimal.valueOf(((Number) expense[1]).doubleValue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        spendingAnalysis.setAmountAvailableTillNow(totalIncomeTillEndDate.subtract(totalExpensesTillEndDate));
        return (spendingAnalysis.getIncomeByCategory().isEmpty() && spendingAnalysis.getExpenseByCategory().isEmpty() && spendingAnalysis.getTotalIncome().compareTo(BigDecimal.ZERO) == 0
                && spendingAnalysis.getTotalExpense().compareTo(BigDecimal.ZERO) == 0 && spendingAnalysis.getAmountAvailableTillNow().compareTo(BigDecimal.ZERO) == 0) ? null : spendingAnalysis;
    }

    @Override
    public byte[] getUserSpendingAnalysisByBudgetCategoriesPdf(Long userId, LocalDate fromDate, LocalDate toDate, String authHeader) {
        UserDetailsForSpendingAnalysisDto userDetails = wealthCoreRepository.getUserDetailsForAccountSpendingAnalysisStatement(userId);
        userDetails.setUsername(makeUsernamePrivate(userDetails.getUsername()));
        return generatePdfTemplate.generatePdf(getUserSpendingAnalysisByBudgetCategories(userId, fromDate, toDate, authHeader), generateDocumentPasswordForUser(userDetails), userDetails, fromDate, toDate);
    }

    @Override
    public ResponseEntity<String> getUserSpendingAnalysisByBudgetCategoriesPdfEmail(Long userId, LocalDate fromDate, LocalDate toDate, String authHeader) {
        try {
            byte[] pdfBytes = getUserSpendingAnalysisByBudgetCategoriesPdf(userId, fromDate, toDate, authHeader);
            apiCallToGatewayServiceToSendEmail(pdfBytes, authHeader);
            return ResponseEntity.ok(EMAIL_SENT_SUCCESS_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(EMAIL_SENT_FAILURE_MESSAGE + ": " + e.getMessage());
        }
    }

    private void apiCallToGatewayServiceToSendEmail(byte[] pdfBytes, String authHeader){
        String token = authHeader.substring(7);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setBearerAuth(token);

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(pdfBytes, headers);
        ResponseEntity<Void> response = restTemplate.exchange(
                StringConstants.USER_SERVICE_URL_CONTROLLER + "/spending-analysis/email",
                HttpMethod.POST,
                requestEntity,
                Void.class
        );
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new ResourceNotFoundException(EMAIL_SENT_FAILURE_MESSAGE + ": " + response.getStatusCode());
        }
    }
}
