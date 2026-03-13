package com.moneyfi.transaction.validator;

import com.moneyfi.transaction.exceptions.GenericException;
import com.moneyfi.transaction.exceptions.ResourceNotFoundException;
import com.moneyfi.transaction.exceptions.ScenarioNotPossibleException;
import com.moneyfi.transaction.service.income.dto.request.TransactionsListRequestDto;
import com.moneyfi.transaction.service.transaction.dto.request.ParsedTransaction;
import com.moneyfi.transaction.service.transaction.dto.response.GmailSyncErrorResponse;
import com.moneyfi.transaction.utils.enums.CreditOrDebit;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.moneyfi.transaction.utils.StringConstants.*;

public class TransactionValidator {

    private TransactionValidator(){}

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void validateTransactionsListGetRequestDto(Long userId, TransactionsListRequestDto requestDto) {
        if(ObjectUtils.isEmpty(userId)) throw new ResourceNotFoundException("User not found");
        validateCategory(requestDto.getCategory());
        validateDate(String.valueOf(requestDto.getDate()));
        validateRequestType(requestDto.getRequestType());
    }

    private static void validateRequestType(String requestType) {
        if (StringUtils.isBlank(requestType)) throw new ScenarioNotPossibleException("Request Type not found");
        if (!"MONTHLY".equalsIgnoreCase(requestType) && !"YEARLY".equalsIgnoreCase(requestType)) throw new ScenarioNotPossibleException("Irrelevant Request Type");
    }

    private static void validateDate(String date) {
        if (StringUtils.isBlank(date)) throw new ScenarioNotPossibleException("Date not found");
        try {
            LocalDate.parse(date, FORMATTER);
        } catch (DateTimeParseException e) {
            throw new ScenarioNotPossibleException("Invalid date format. Expected format: yyyy-MM-dd");
        }
    }

    private static void validateCategory(String category) {
        if(StringUtils.isBlank(category)) throw new ScenarioNotPossibleException("Category not found");
        if (!ALL.equalsIgnoreCase(category) && !category.matches("\\d+")) {
            throw new ScenarioNotPossibleException("Invalid category. Must be 'ALL' or a numeric category id");
        }
    }

    public static void validateGmailSyncTransactionsBulkUpload(List<ParsedTransaction> transactions, List<Integer> incomeCategoryIds, List<Integer> expenseCategoryIds) throws GenericException {
        List<GmailSyncErrorResponse> errorList = new ArrayList<>();
        for (ParsedTransaction transaction : transactions) {
            GmailSyncErrorResponse dto = new GmailSyncErrorResponse();
            Map<String, List<String>> map = new HashMap<>();

            validateTransactionCategory(transaction, map, incomeCategoryIds, expenseCategoryIds);
            validateTransactionDescription(transaction, map);
            validateTransactionAmount(transaction, map);
            validateTransactionDateAndTime(transaction, map);

            if (!map.isEmpty()) {
                dto.setGmailProcessedId(transaction.getGmailProcessedId());
                dto.setErrorColumns(List.of(map));
            }
            if (ObjectUtils.isNotEmpty(dto.getGmailProcessedId())) errorList.add(dto);
        }
        if (!errorList.isEmpty()) throw GenericException.create(errorList);
    }

    private static void validateTransactionDateAndTime(ParsedTransaction transaction, Map<String, List<String>> map) {
        if (transaction.getTransactionDate() == null) {
            map.computeIfAbsent("transactionDate", k -> new ArrayList<>()).add("Date can't be empty");
        }
    }

    private static void validateTransactionAmount(ParsedTransaction transaction, Map<String, List<String>> map) {
        if (ObjectUtils.isEmpty(transaction.getAmount())) {
            map.computeIfAbsent("amount", k -> new ArrayList<>()).add("Amount can't be empty");
        }
        if (!ObjectUtils.isEmpty(transaction.getAmount()) && transaction.getAmount().compareTo(BigDecimal.ZERO) == 0) {
            map.computeIfAbsent("amount", k -> new ArrayList<>()).add("Amount can't be zero");
        }
    }

    private static void validateTransactionDescription(ParsedTransaction transaction, Map<String, List<String>> map) {
        if (StringUtils.isBlank(transaction.getDescription())) {
            map.computeIfAbsent("description", k -> new ArrayList<>()).add("Description can't be empty");
        }
        if (!StringUtils.isBlank(transaction.getDescription()) && !transaction.getDescription().substring(0,1).equals(transaction.getDescription().substring(0,1).toUpperCase())) {
            map.computeIfAbsent("description", k -> new ArrayList<>()).add("First letter should be in capital");
        }
    }

    private static void validateTransactionCategory(ParsedTransaction transaction, Map<String, List<String>> map, List<Integer> incomeCategoryIds, List<Integer> expenseCategoryIds) {
        if (ObjectUtils.isEmpty(transaction.getCategoryId())) {
            map.computeIfAbsent("categoryId", k -> new ArrayList<>()).add("Category can't be empty");
        }

        List<Integer> categoryIds = transaction.getTransactionType().equalsIgnoreCase(CreditOrDebit.CREDIT.name()) ? incomeCategoryIds : expenseCategoryIds;
        if (!ObjectUtils.isEmpty(transaction.getCategoryId()) && !categoryIds.contains(transaction.getCategoryId())) {
            map.computeIfAbsent("categoryId", k -> new ArrayList<>()).add(CATEGORY_NOT_ALIGN_MESSAGE);
        }
    }
}
