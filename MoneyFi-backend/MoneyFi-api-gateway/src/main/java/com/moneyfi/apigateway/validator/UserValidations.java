package com.moneyfi.apigateway.validator;

import com.moneyfi.apigateway.exceptions.ScenarioNotPossibleException;
import com.moneyfi.apigateway.model.auth.UserAuthModel;
import com.moneyfi.apigateway.repository.user.auth.UserRepository;
import com.moneyfi.apigateway.service.maintainer.dto.request.CreateOrUpdateAdminRequestDto;
import com.moneyfi.apigateway.service.userservice.dto.request.ChangePasswordDto;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.authentication.BadCredentialsException;

import static com.moneyfi.apigateway.util.constants.StringConstants.*;

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

    public static void validateAdminCreationRequestByMaintainer(CreateOrUpdateAdminRequestDto requestDto) {
        if(ObjectUtils.isEmpty(requestDto.getUsername())) {
            throw new ScenarioNotPossibleException("Username is required");
        }
        if(ObjectUtils.isEmpty(requestDto.getPassword())) {
            throw new ScenarioNotPossibleException("Password is required");
        }
    }

    public static void validateExistingUserCheck(CreateOrUpdateAdminRequestDto requestDto, UserRepository userRepository) {
        UserAuthModel existingUserCheck = userRepository.getUserDetailsByUsername(requestDto.getUsername().trim()).orElse(null);
        if (existingUserCheck != null) {
            if (existingUserCheck.isDeleted()) throw new ScenarioNotPossibleException("Username matching with out previous records. Please choose different username");
            if (existingUserCheck.isBlocked()) throw new ScenarioNotPossibleException("There is one blocked user with this username. Please choose different one");
            else throw new ScenarioNotPossibleException(USER_ALREADY_EXISTING_MESSAGE);
        }
    }
}
