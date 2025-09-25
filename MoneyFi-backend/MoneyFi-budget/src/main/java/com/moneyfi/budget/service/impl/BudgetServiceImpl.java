package com.moneyfi.budget.service.impl;

import com.moneyfi.budget.exceptions.ResourceNotFoundException;
import com.moneyfi.budget.model.BudgetModel;
import com.moneyfi.budget.repository.BudgetRepository;
import com.moneyfi.budget.repository.common.BudgetCommonRepository;
import com.moneyfi.budget.service.BudgetService;
import com.moneyfi.budget.service.dto.request.AddBudgetDto;
import com.moneyfi.budget.service.dto.response.BudgetDetailsDto;
import com.moneyfi.budget.utils.StringConstants;
import jakarta.transaction.Transactional;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;


@Service
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetCommonRepository budgetCommonRepository;
    private final RestTemplate restTemplate;

    public BudgetServiceImpl(BudgetRepository budgetRepository,
                             BudgetCommonRepository budgetCommonRepository,
                             RestTemplate restTemplate){
        this.budgetRepository = budgetRepository;
        this.budgetCommonRepository = budgetCommonRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    @Transactional
    public void saveBudget(List<AddBudgetDto> budgetList, Long userId) {

        for(AddBudgetDto budget : budgetList){
            BudgetModel budgetModel = new BudgetModel();
            budgetModel.setUserId(userId);
            budgetModel.setCategory(budget.getCategory());
            budgetModel.setMoneyLimit(budget.getMoneyLimit());
            budgetRepository.save(budgetModel);
        }
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
    public BigDecimal getUserSpendingAnalysisByBudgetCategories(Long userId, LocalDate fromDate, LocalDate toDate, String authHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", authHeader);
        HttpEntity<Object> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<BigDecimal> incomeResponse = restTemplate.exchange(
                StringConstants.EUREKA_INCOME_SERVICE_URL + "/total-income/specified-range?fromDate=" + fromDate + "&toDate=" + toDate,
                HttpMethod.GET,
                requestEntity,
                BigDecimal.class
        );

        ResponseEntity<BigDecimal> expenseResponse = restTemplate.exchange(
                StringConstants.EUREKA_EXPENSE_SERVICE_URL + "/total-expenses/specified-range?fromDate=" + fromDate + "&toDate=" + toDate,
                HttpMethod.GET,
                requestEntity,
                BigDecimal.class
        );
        return expenseResponse.getBody();
    }
}
