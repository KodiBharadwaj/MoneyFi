package com.moneyfi.transaction.validator;

import com.moneyfi.transaction.exceptions.ResourceNotFoundException;
import com.moneyfi.transaction.exceptions.ScenarioNotPossibleException;
import com.moneyfi.transaction.service.income.dto.request.TransactionsListRequestDto;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import static com.moneyfi.transaction.utils.StringConstants.ALL;

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
}
