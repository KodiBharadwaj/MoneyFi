package com.moneyfi.apigateway.util.validators;

import com.moneyfi.apigateway.exceptions.ScenarioNotPossibleException;
import com.moneyfi.apigateway.model.auth.OtpTempModel;
import com.moneyfi.apigateway.model.auth.UserAuthModel;
import com.moneyfi.apigateway.service.userservice.dto.request.AccountBlockOrDeleteRequestDto;
import com.moneyfi.apigateway.service.userservice.dto.request.ChangePasswordDto;
import com.moneyfi.apigateway.util.constants.StringUtils;
import com.moneyfi.apigateway.util.enums.AccDeactivationType;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.LocalDateTime;

import static com.moneyfi.apigateway.util.constants.StringUtils.*;

public class UserValidations {

    private UserValidations() {};

    private static final String INPUT_FIELDS_SHOULD_NOT_EMPTY_MESSAGE = "Input fields should not be empty";
    private static final String USER_UNABLE_OPERATIONS = "User is not active to perform the operation";
    private static final String PLEASE_ENTER_CORRECT_OTP = "Please enter correct otp";
    private static final String OTP_EXPIRED_MESSAGE = "Otp expired, Try new one";
    private static final String INCORRECT_PASSWORD_MESSAGE = "Incorrect Password entered!";
    private static final String DESCRIPTION_MANDATORY_MESSAGE = "Description is mandatory";
    private static final String PASSWORD_CHANGE_LIMIT_CROSSED_MESSAGE = "Your Password change limit reached for today, Try tomorrow";

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

    public static void changePasswordValidations(ChangePasswordDto changePasswordDto, UserAuthModel user) {
        if (changePasswordDto.getDescription() == null || changePasswordDto.getDescription().trim().isEmpty()) {
            throw new ScenarioNotPossibleException(DESCRIPTION_MANDATORY_MESSAGE);
        }
        if (!encoder.matches(changePasswordDto.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException(INCORRECT_OLD_PASSWORD);
        } else if (user.getOtpCount() >= 3) {
            throw new ScenarioNotPossibleException(PASSWORD_CHANGE_LIMIT_CROSSED_MESSAGE);
        }
    }

    public static void checkPhoneNumberValidations(String phone) {
        if (!phone.matches("\\d+")) {
            throw new ScenarioNotPossibleException(StringUtils.PHONE_NUMBER_DIGITS_ONLY_MESSAGE);
        }
        if (phone.length() != 10) {
            throw new ScenarioNotPossibleException(StringUtils.PHONE_NUMBER_MAX_LENGTH_MESSAGE);
        }
    }

    public static void checkForUserAlreadyExistenceValidation(UserAuthModel user) {
        if(user != null){
            throw new ScenarioNotPossibleException(USER_ALREADY_EXISTING_MESSAGE);
        }
    }

    public static void otpSendToUserDuringSignupValidation(UserAuthModel user) {
        userAlreadyDeactivatedCheckValidation(user);
        if(user.getOtpCount() >= 3){
            throw new ScenarioNotPossibleException(EMAIL_LIMIT_CROSSED);
        }
    }
}
