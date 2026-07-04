package com.moneyfi.transaction.utils;

import com.moneyfi.transaction.service.expense.dto.response.ExpenseDetailsDto;
import com.moneyfi.transaction.service.income.dto.request.TransactionsListRequestDto;
import com.moneyfi.transaction.service.income.dto.response.IncomeDetailsDto;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.moneyfi.transaction.utils.constants.StringConstants.*;
import static com.moneyfi.transaction.utils.constants.StringConstants.DESC;
import static com.moneyfi.transaction.utils.constants.StringConstants.SOURCE;
import static com.moneyfi.transaction.utils.constants.StringConstants.TYPE;

@UtilityClass
public class TransactionUtilService {

    public <T> List<T> returnSortedTransactionResponse(TransactionsListRequestDto requestDto, List<T> transactions, Map<String, Comparator<T>> comparators) {
        Comparator<T> comparator = comparators.get(requestDto.getSortBy().toLowerCase());

        if (comparator == null) {
            return transactions;
        }

        if (DESC.equalsIgnoreCase(requestDto.getSortOrder())) {
            comparator = comparator.reversed();
        }
        return transactions.stream().sorted(comparator).toList();
    }

    public static final Map<String, Comparator<IncomeDetailsDto>>
            INCOME_COMPARATORS = createTransactionComparators(
            IncomeDetailsDto::getCategory,
            IncomeDetailsDto::getDate,
            IncomeDetailsDto::getAmount,
            IncomeDetailsDto::isRecurring,
            SOURCE,
            IncomeDetailsDto::getSource
    );

    public static final Map<String, Comparator<ExpenseDetailsDto>>
            EXPENSE_COMPARATORS = createTransactionComparators(
            ExpenseDetailsDto::getCategory,
            ExpenseDetailsDto::getDate,
            ExpenseDetailsDto::getAmount,
            ExpenseDetailsDto::isRecurring,
            DESCRIPTION,
            ExpenseDetailsDto::getDescription
    );

    private <T> Map<String, Comparator<T>> createTransactionComparators(Function<T, String> categoryExtractor,
                                                                        Function<T, Date> dateExtractor,
                                                                        Function<T, BigDecimal> amountExtractor,
                                                                        Function<T, Boolean> recurringExtractor,
                                                                        String extraSortKey,
                                                                        Function<T, String> extraFieldExtractor) {
        return Map.of(
                CATEGORY, Comparator.comparing(categoryExtractor, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)),
                DATE, Comparator.comparing(dateExtractor, Comparator.nullsLast(Date::compareTo)),
                AMOUNT, Comparator.comparing(amountExtractor, Comparator.nullsLast(BigDecimal::compareTo)),
                TYPE, Comparator.comparing(recurringExtractor, Comparator.nullsLast(Boolean::compareTo)),
                extraSortKey,
                Comparator.comparing(extraFieldExtractor, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
        );
    }
}