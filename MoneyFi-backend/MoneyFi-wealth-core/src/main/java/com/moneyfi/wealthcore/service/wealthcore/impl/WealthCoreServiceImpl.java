package com.moneyfi.wealthcore.service.wealthcore.impl;

import com.moneyfi.wealthcore.repository.common.WealthCoreRepository;
import com.moneyfi.wealthcore.service.api.ExternalApiCallService;
import com.moneyfi.wealthcore.service.budget.dto.response.SpendingAnalysisResponseDto;
import com.moneyfi.wealthcore.service.budget.dto.response.UserDetailsForSpendingAnalysisDto;
import com.moneyfi.wealthcore.service.wealthcore.WealthCoreService;
import com.moneyfi.wealthcore.utils.GeneratePdfTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.moneyfi.wealthcore.utils.constants.StringConstants.*;
import static com.moneyfi.wealthcore.utils.constants.StringConstants.EMAIL_SENT_FAILURE_MESSAGE;

@Service
@RequiredArgsConstructor
public class WealthCoreServiceImpl implements WealthCoreService {

    private final WealthCoreRepository wealthCoreRepository;
    private final GeneratePdfTemplate generatePdfTemplate;
    private final ExternalApiCallService externalApiCallService;

    @Override
    public SpendingAnalysisResponseDto getUserSpendingAnalysisByBudgetCategories(Long userId, LocalDate fromDate, LocalDate toDate, String authHeader) {
        SpendingAnalysisResponseDto spendingAnalysis = new SpendingAnalysisResponseDto(new HashMap<>(), new HashMap<>(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        List<Object[]> incomeResponse = externalApiCallService.externalApiCallToTransactionService(
                authHeader,
                Map.of(
                        FROM_DATE, fromDate.toString(),
                        TO_DATE, toDate.toString()
                ),
                "/income/total-income/specified-range"
        );
        List<Object[]> expenseResponse = externalApiCallService.externalApiCallToTransactionService(
                authHeader,
                Map.of(
                        FROM_DATE, fromDate.toString(),
                        TO_DATE, toDate.toString()
                ),
                "/expense/total-expenses/specified-range"
        );
        List<Object[]> incomeResponseTillToDate = externalApiCallService.externalApiCallToTransactionService(
                authHeader,
                Map.of(
                        FROM_DATE, LocalDate.of(1, 1, 1).toString(),
                        TO_DATE, toDate.toString()
                ),
                "/income/total-income/specified-range"
        );
        List<Object[]> expenseResponseTillToDate = externalApiCallService.externalApiCallToTransactionService(
                authHeader,
                Map.of(
                        FROM_DATE, LocalDate.of(1, 1, 1).toString(),
                        TO_DATE, toDate.toString()
                ),
                "/expense/total-expenses/specified-range"
        );

        Map<String, BigDecimal> incomeByCategoryMap = new HashMap<>();
        Objects.requireNonNull(incomeResponse)
                .forEach(income -> {
                    String category = (String) income[0];
                    BigDecimal amount = BigDecimal.valueOf(((Number) income[1]).doubleValue());
                    incomeByCategoryMap.put(category, amount);
                    spendingAnalysis.setTotalIncome(spendingAnalysis.getTotalIncome().add(amount));
                });
        spendingAnalysis.setIncomeByCategory(incomeByCategoryMap);

        Map<String, BigDecimal> expenseByCategoryMap = new HashMap<>();
        Objects.requireNonNull(expenseResponse)
                .forEach(expense -> {
                    String category = (String) expense[0];
                    BigDecimal amount = BigDecimal.valueOf(((Number) expense[1]).doubleValue());
                    expenseByCategoryMap.put(category, amount);
                    spendingAnalysis.setTotalExpense(spendingAnalysis.getTotalExpense().add(amount));
                });
        spendingAnalysis.setExpenseByCategory(expenseByCategoryMap);

        BigDecimal totalIncomeTillEndDate = Objects.requireNonNull(incomeResponseTillToDate)
                .stream()
                .map(income -> BigDecimal.valueOf(((Number) income[1]).doubleValue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpensesTillEndDate = Objects.requireNonNull(expenseResponseTillToDate)
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
            externalApiCallService.externalApiCallToUserServiceToSendPdfEmail(pdfBytes, authHeader, "/spending-analysis/email");
            return ResponseEntity.ok(EMAIL_SENT_SUCCESS_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(EMAIL_SENT_FAILURE_MESSAGE + ": " + e.getMessage());
        }
    }
}
