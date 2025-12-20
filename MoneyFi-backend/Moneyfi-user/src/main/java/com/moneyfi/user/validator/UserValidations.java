package com.moneyfi.user.validator;

import com.moneyfi.user.exceptions.ScenarioNotPossibleException;
import com.moneyfi.user.model.dto.OtpTempModel;
import com.moneyfi.user.model.dto.UserAuthModel;
import com.moneyfi.user.service.common.dto.request.AccountBlockOrDeleteRequestDto;
import com.moneyfi.user.util.constants.StringUtils;
import com.moneyfi.user.util.enums.AccDeactivationType;

import java.time.LocalDateTime;

public class UserValidations {

    private UserValidations() {}

    private static final String INPUT_FIELDS_SHOULD_NOT_EMPTY_MESSAGE = "Input fields should not be empty";
    private static final String USER_UNABLE_OPERATIONS = "User is not active to perform the operation";
    private static final String PLEASE_ENTER_CORRECT_OTP = "Please enter correct otp";
    private static final String OTP_EXPIRED_MESSAGE = "Otp expired, Try new one";
    private static final String INCORRECT_PASSWORD_MESSAGE = "Incorrect Password entered!";

    public static void checkPhoneNumberValidations(String phone) {
        if (!phone.matches("\\d+")) {
            throw new ScenarioNotPossibleException(StringUtils.PHONE_NUMBER_DIGITS_ONLY_MESSAGE);
        }
        if (phone.length() != 10) {
            throw new ScenarioNotPossibleException(StringUtils.PHONE_NUMBER_MAX_LENGTH_MESSAGE);
        }
    }

    public static void userAccountDeactivationInputValidation(AccountBlockOrDeleteRequestDto request) {
        if (request.getOtp() == null || request.getOtp().isEmpty() || request.getDescription() == null || request.getDescription().isEmpty()) {
            throw new ScenarioNotPossibleException(INPUT_FIELDS_SHOULD_NOT_EMPTY_MESSAGE);
        }
    }

    public static void userAlreadyDeactivatedCheckValidation(UserAuthModel user) {
        if (user.isBlocked() || user.isDeleted()) {
            throw new ScenarioNotPossibleException(USER_UNABLE_OPERATIONS);
        }
    }

    public static void otpCheckDuringAccountDeactivationValidations(AccountBlockOrDeleteRequestDto request, OtpTempModel response, UserAuthModel user) {
        if (!response.getOtp().equals(request.getOtp())) {
            throw new ScenarioNotPossibleException(PLEASE_ENTER_CORRECT_OTP);
        }
        if (response.getExpirationTime().isBefore(LocalDateTime.now())) {
            throw new ScenarioNotPossibleException(OTP_EXPIRED_MESSAGE);
        }
        if (request.getDeactivationType().equalsIgnoreCase(AccDeactivationType.DELETE.name())) {
            String userPassword = request.getPassword();
            if (userPassword == null || userPassword.isEmpty() || !StringUtils.encoder.matches(userPassword, user.getPassword())) {
                throw new ScenarioNotPossibleException(INCORRECT_PASSWORD_MESSAGE);
            }
        }
    }
}
