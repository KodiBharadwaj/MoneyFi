package com.moneyfi.wealthcore.service.budget.impl;

import com.moneyfi.wealthcore.exceptions.ResourceNotFoundException;
import com.moneyfi.wealthcore.exceptions.ScenarioNotPossibleException;
import com.moneyfi.wealthcore.model.budget.BudgetModel;
import com.moneyfi.wealthcore.model.common.CategoryListModel;
import com.moneyfi.wealthcore.repository.budget.BudgetRepository;
import com.moneyfi.wealthcore.repository.common.CategoryListRepository;
import com.moneyfi.wealthcore.repository.common.WealthCoreRepository;
import com.moneyfi.wealthcore.service.budget.BudgetService;
import com.moneyfi.wealthcore.service.budget.dto.request.AddBudgetDto;
import com.moneyfi.wealthcore.service.budget.dto.response.BudgetDetailsDto;
import com.moneyfi.wealthcore.utils.enums.TransactionServiceType;
import com.moneyfi.wealthcore.validator.BudgetValidator;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

import static com.moneyfi.wealthcore.utils.StringConstants.*;

@Service
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final WealthCoreRepository wealthCoreRepository;
    private final CategoryListRepository categoryListRepository;

    public BudgetServiceImpl(BudgetRepository budgetRepository,
                             WealthCoreRepository wealthCoreRepository,
                             CategoryListRepository categoryListRepository) {
        this.budgetRepository = budgetRepository;
        this.wealthCoreRepository = wealthCoreRepository;
        this.categoryListRepository = categoryListRepository;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void saveBudget(List<AddBudgetDto> budgetList, Long userId) {
        Optional<List<BudgetModel>> existingBudget = budgetRepository.findByUserId(userId);
        if(existingBudget.isPresent() && !existingBudget.get().isEmpty()) {
            throw new ScenarioNotPossibleException("Budget already exists! Please update if required");
        }
        BudgetValidator.validateBudgetSaveRequestDto(budgetList, getTotalIncomeInMonthAndYear(userId, LocalDateTime.now().getMonthValue(), LocalDateTime.now().getYear()));
        Set<Integer> existingCategoryIds = new HashSet<>(categoryListRepository.findByType(TransactionServiceType.EXPENSE.name()).stream().map(CategoryListModel::getId).toList());
        if (!budgetList
                .stream()
                .map(AddBudgetDto::getCategoryId)
                .allMatch(existingCategoryIds::contains)
        ) {
            throw new ScenarioNotPossibleException(CATEGORY_ID_INVALID);
        }
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
        return wealthCoreRepository.getBudgetsByUserId(userId, month, year, category);
    }

    @Override
    public BigDecimal budgetProgress(Long userId, int month, int year) {
        return getTotalExpenseInMonthAndYear(userId, month, year).divide(getAllBudgetsByUserIdAndCategory(userId, month, year, "all")
                .stream()
                .map(BudgetDetailsDto::getMoneyLimit)
                .reduce(BigDecimal.ZERO, BigDecimal::add), 5, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void updateBudget(Long userId, List<BudgetModel> budgetList) {
        BudgetValidator.validateBudgetUpdateRequestDto(budgetList);
        List<BudgetModel> budgetListToUpdate = new ArrayList<>();
        LocalDateTime currentTime = LocalDateTime.now();
        for (BudgetModel budget : budgetList) {
            BudgetModel budgetModel = budgetRepository.findById(budget.getId()).orElseThrow(() -> new ResourceNotFoundException(BUDGET_NOT_FOUND));
            if (budget.getMoneyLimit().compareTo(BigDecimal.ZERO) >= 0) {
                budgetModel.setMoneyLimit(budget.getMoneyLimit());
            }
            budgetModel.setUpdatedAt(currentTime);
            budgetListToUpdate.add(budgetModel);
        }
        budgetRepository.saveAll(budgetListToUpdate);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void deleteBudget(Long userId) {
        Optional<List<BudgetModel>> budgetList = budgetRepository.findByUserId(userId);
        if (budgetList.isPresent() && !budgetList.get().isEmpty()) {
            budgetRepository.deleteAll(budgetList.get());
        } else {
            throw new ResourceNotFoundException(BUDGET_NOT_FOUND);
        }
    }

    private BigDecimal getTotalIncomeInMonthAndYear(Long userId, int month, int year) {
        BigDecimal totalIncome = budgetRepository.getTotalIncomeInMonthAndYear(userId, month, year);
        if(totalIncome == null){
            return BigDecimal.ZERO;
        }
        return totalIncome;
    }

    private BigDecimal getTotalExpenseInMonthAndYear(Long userId, int month, int year) {
        BigDecimal totalExpense = budgetRepository.getTotalExpenseInMonthAndYear(userId, month, year);
        if(totalExpense == null){
            return BigDecimal.ZERO;
        }
        return totalExpense;
    }
}
