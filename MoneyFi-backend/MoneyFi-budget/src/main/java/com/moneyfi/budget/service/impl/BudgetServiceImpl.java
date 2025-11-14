package com.moneyfi.budget.service.impl;

import com.moneyfi.budget.exceptions.ResourceNotFoundException;
import com.moneyfi.budget.model.BudgetModel;
import com.moneyfi.budget.repository.BudgetRepository;
import com.moneyfi.budget.repository.common.BudgetCommonRepository;
import com.moneyfi.budget.service.BudgetService;
import com.moneyfi.budget.service.dto.request.AddBudgetDto;
import com.moneyfi.budget.service.dto.response.BudgetDetailsDto;
import com.moneyfi.budget.service.dto.response.SpendingAnalysisResponseDto;
import com.moneyfi.budget.service.dto.response.UserDetailsForSpendingAnalysisDto;
import com.moneyfi.budget.utils.GeneratePdfTemplate;
import com.moneyfi.budget.utils.StringConstants;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

import static com.moneyfi.budget.utils.StringConstants.BUDGET_NOT_FOUND;


@Service
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetCommonRepository budgetCommonRepository;
    private final RestTemplate restTemplate;
    private final GeneratePdfTemplate generatePdfTemplate;

    public BudgetServiceImpl(BudgetRepository budgetRepository,
                             BudgetCommonRepository budgetCommonRepository,
                             RestTemplate restTemplate,
                             GeneratePdfTemplate generatePdfTemplate) {
        this.budgetRepository = budgetRepository;
        this.budgetCommonRepository = budgetCommonRepository;
        this.restTemplate = restTemplate;
        this.generatePdfTemplate = generatePdfTemplate;
    }

    @Override
    @Transactional
    public void saveBudget(List<AddBudgetDto> budgetList, Long userId) {
        List<BudgetModel> newBudget = new ArrayList<>();
        for (AddBudgetDto budget : budgetList) {
            BudgetModel budgetModel = new BudgetModel();
            BeanUtils.copyProperties(budget, budgetModel);
            budgetModel.setUserId(userId);
            newBudget.add(budgetModel);
        }
        budgetRepository.saveAll(newBudget);
    }

    @Override
    public List<BudgetDetailsDto> getAllBudgetsByUserIdAndCategory(Long userId, int month, int year, String category) {
        return budgetCommonRepository.getBudgetsByUserId(userId, month, year, category);
    }

    @Override
    public BigDecimal budgetProgress(Long userId, int month, int year) {

        List<BudgetDetailsDto> budgetsList = getAllBudgetsByUserIdAndCategory(userId, month, year, "all");
        BigDecimal moneyLimit = budgetsList
                            .stream()
                            .map(i->i.getMoneyLimit())
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal currentSpending = getTotalExpenseInMonthAndYear(userId, month, year);

        return currentSpending.divide(moneyLimit, 5, RoundingMode.HALF_UP);
    }

    private BigDecimal getTotalExpenseInMonthAndYear(Long userId, int month, int year) {
        BigDecimal totalExpense = budgetRepository.getTotalExpenseInMonthAndYear(userId, month, year);
        if(totalExpense == null){
            return BigDecimal.ZERO;
        }

        return totalExpense;
    }

    @Override
    @Transactional
    public void updateBudget(Long userId, List<BudgetModel> budgetList) {

        for(BudgetModel budget : budgetList){
            BudgetModel budgetModel = budgetRepository.findById(budget.getId()).orElse(null);

            if(budgetModel == null || !budgetModel.getUserId().equals(userId)){
                throw new ResourceNotFoundException("UnAuthorized try");
            }

            if(budget.getCategory() != null){
                budgetModel.setCategory(budget.getCategory());
            }
            if(budget.getCurrentSpending().compareTo(BigDecimal.ZERO) > 0){
                budgetModel.setCurrentSpending(budget.getCurrentSpending());
            }
            if(budget.getMoneyLimit().compareTo(BigDecimal.ZERO) > 0){
                budgetModel.setMoneyLimit(budget.getMoneyLimit());
            }

            budgetRepository.save(budgetModel);
        }
    }

    @Override
    public SpendingAnalysisResponseDto getUserSpendingAnalysisByBudgetCategories(Long userId, LocalDate fromDate, LocalDate toDate, String authHeader) {
        SpendingAnalysisResponseDto spendingAnalysis = new SpendingAnalysisResponseDto(new HashMap<>(), new HashMap<>(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", authHeader);
        HttpEntity<Object> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<List<Object[]>> incomeResponse = restTemplate.exchange(
                StringConstants.EUREKA_INCOME_SERVICE_URL + "/total-income/specified-range?fromDate=" + fromDate + "&toDate=" + toDate,
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<List<Object[]>>() {}
        );
        ResponseEntity<List<Object[]>> expenseResponse = restTemplate.exchange(
                StringConstants.EUREKA_EXPENSE_SERVICE_URL + "/total-expenses/specified-range?fromDate=" + fromDate + "&toDate=" + toDate,
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<List<Object[]>>() {}
        );
        ResponseEntity<List<Object[]>> incomeResponseTillToDate = restTemplate.exchange(
                StringConstants.EUREKA_INCOME_SERVICE_URL + "/total-income/specified-range?fromDate=" + LocalDate.of(1, 1, 1) + "&toDate=" + toDate,
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<List<Object[]>>() {}
        );
        ResponseEntity<List<Object[]>> expenseResponseTillToDate = restTemplate.exchange(
                StringConstants.EUREKA_EXPENSE_SERVICE_URL + "/total-expenses/specified-range?fromDate=" + LocalDate.of(1, 1, 1) + "&toDate=" + toDate,
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
        UserDetailsForSpendingAnalysisDto userDetails = budgetCommonRepository.getUserDetailsForAccountSpendingAnalysisStatement(userId);
        userDetails.setUsername(makeUsernamePrivate(userDetails.getUsername()));
        return generatePdfTemplate.generatePdf(getUserSpendingAnalysisByBudgetCategories(userId, fromDate, toDate, authHeader), generateDocumentPasswordForUser(userDetails), userDetails, fromDate, toDate);
    }

    @Override
    public ResponseEntity<String> getUserSpendingAnalysisByBudgetCategoriesPdfEmail(Long userId, LocalDate fromDate, LocalDate toDate, String authHeader) {
        try {
            byte[] pdfBytes = getUserSpendingAnalysisByBudgetCategoriesPdf(userId, fromDate, toDate, authHeader);
            apiCallToGatewayServiceToSendEmail(pdfBytes, authHeader);
            return ResponseEntity.ok("Email sent successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send email: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteBudget(Long userId) {
        Optional<List<BudgetModel>> budgetList = budgetRepository.findByUserId(userId);
        if (budgetList.isPresent()) {
            budgetRepository.deleteAll(budgetList.get());
        } else {
            throw new ResourceNotFoundException(BUDGET_NOT_FOUND);
        }
    }

    private void apiCallToGatewayServiceToSendEmail(byte[] pdfBytes, String authHeader){
        String token = authHeader.substring(7);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setBearerAuth(token);

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(pdfBytes, headers);
        ResponseEntity<Void> response = restTemplate.exchange(
                StringConstants.API_GATEWAY_URL_PROFILE_CONTROLLER + "spending-analysis/email",
                HttpMethod.POST,
                requestEntity,
                Void.class
        );
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new ResourceNotFoundException("Failed to send email: " + response.getStatusCode());
        }
    }

    private String makeUsernamePrivate(String username){
        int index = username.indexOf('@');
        return username.substring(0, index/3) +
                "x".repeat(index - index/3) + username.substring(index);
    }

    private String generateDocumentPasswordForUser(UserDetailsForSpendingAnalysisDto userDetails){
        return userDetails.getName().substring(0,4).toUpperCase() +
                userDetails.getUsername().substring(0,4).toLowerCase();
    }
}
