package com.moneyfi.transaction.validator;

import com.moneyfi.transaction.exceptions.ScenarioNotPossibleException;
import com.moneyfi.transaction.service.income.dto.request.IncomeSaveRequest;
import com.moneyfi.transaction.service.income.dto.request.IncomeUpdateRequest;
import org.apache.commons.lang3.ObjectUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class IncomeValidator {

    private IncomeValidator () {}

    private static final String INCOMPLETE_FIELDS = "Please fill all the fields";
    private static final String INVALID_AMOUNT = "Please enter valid amount";
    private static final String INVALID_DATE = "Please enter valid date";

    public static void validateIncomeSaveRequest(IncomeSaveRequest incomeSaveRequest, Long userId) {
        validateUserIdNonNull(userId);
        if(incomeSaveRequest.getSource() == null || incomeSaveRequest.getSource().trim().isEmpty()
                || incomeSaveRequest.getCategory() == null || incomeSaveRequest.getCategory().trim().isEmpty()
                || incomeSaveRequest.getAmount() == null || incomeSaveRequest.getDate() == null || incomeSaveRequest.getRecurring() == null) {
            throw new ScenarioNotPossibleException(INCOMPLETE_FIELDS);
        }
        if(incomeSaveRequest.getAmount().compareTo(BigDecimal.ZERO) == 0) throw new ScenarioNotPossibleException(INVALID_AMOUNT);
        if(isValidDateTime(incomeSaveRequest.getDate())) throw new ScenarioNotPossibleException(INVALID_DATE);
    }

    private static void validateUserIdNonNull(Long userId) {
        if(ObjectUtils.isEmpty(userId)) {
            throw new ScenarioNotPossibleException("User Id is empty");
        }
    }

    public static void validateIncomeUpdateRequest(IncomeUpdateRequest incomeUpdateRequest) {
        if(incomeUpdateRequest.getSource() == null || incomeUpdateRequest.getSource().trim().isEmpty()
                || incomeUpdateRequest.getCategory() == null || incomeUpdateRequest.getCategory().trim().isEmpty()
                || incomeUpdateRequest.getAmount() == null || incomeUpdateRequest.getDate() == null || incomeUpdateRequest.getRecurring() == null) {
            throw new ScenarioNotPossibleException(INCOMPLETE_FIELDS);
        }
        if(incomeUpdateRequest.getAmount().compareTo(BigDecimal.ZERO) == 0) throw new ScenarioNotPossibleException(INVALID_AMOUNT);
        if(isValidDateTime(incomeUpdateRequest.getDate())) throw new ScenarioNotPossibleException(INVALID_DATE);
    }

    public static boolean isValidDateTime(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        try {
            LocalDateTime.parse(date, formatter);
            return false;
        } catch (DateTimeParseException e) {
            return true;
        }
    }

}
