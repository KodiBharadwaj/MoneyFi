package com.moneyfi.transaction.service.admin.impl;

import com.moneyfi.transaction.model.expense.ExpenseModel;
import com.moneyfi.transaction.model.income.IncomeModel;
import com.moneyfi.transaction.repository.expense.ExpenseRepository;
import com.moneyfi.transaction.repository.income.IncomeRepository;
import com.moneyfi.transaction.service.admin.AdminService;
import com.moneyfi.transaction.service.admin.dto.response.TransactionCategoryResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;

    public AdminServiceImpl(IncomeRepository incomeRepository,
                            ExpenseRepository expenseRepository) {
        this.incomeRepository = incomeRepository;
        this.expenseRepository = expenseRepository;
    }

    @Override
    public TransactionCategoryResponse getCategoryWiseTransactionSummary() {
        Map<String, BigDecimal> incomeCategory = incomeRepository.findAll()
                .stream()
                .filter(income -> !income.isDeleted())
                .collect(Collectors.groupingBy(IncomeModel::getCategory, Collectors.reducing(BigDecimal.ZERO, IncomeModel::getAmount, BigDecimal::add)));

        Map<String, BigDecimal> expenseCategory = expenseRepository.findAll()
                .stream()
                .filter(expense -> !expense.isDeleted())
                .collect(Collectors.groupingBy(ExpenseModel::getCategory, Collectors.reducing(BigDecimal.ZERO, ExpenseModel::getAmount, BigDecimal::add)));

        return new TransactionCategoryResponse(incomeCategory, expenseCategory);
    }
}
