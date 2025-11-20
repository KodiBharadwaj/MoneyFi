package com.moneyfi.apigateway.util.validators;

import com.moneyfi.apigateway.exceptions.ScenarioNotPossibleException;
import com.moneyfi.apigateway.model.auth.UserAuthModel;
import com.moneyfi.apigateway.service.userservice.dto.request.ChangePasswordDto;
import org.springframework.security.authentication.BadCredentialsException;

import static com.moneyfi.apigateway.util.constants.StringUtils.*;

public class UserValidations {

    private UserValidations() {}

    private static final String USER_UNABLE_OPERATIONS = "User is not active to perform the operation";
    private static final String DESCRIPTION_MANDATORY_MESSAGE = "Description is mandatory";
    private static final String PASSWORD_CHANGE_LIMIT_CROSSED_MESSAGE = "Your Password change limit reached for today, Try tomorrow";

    public static void userAlreadyDeactivatedCheckValidation(UserAuthModel user) {
        if (user.isBlocked() || user.isDeleted()) {
            throw new ScenarioNotPossibleException(USER_UNABLE_OPERATIONS);
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
