package com.moneyfi.user.validator;

import com.moneyfi.user.exceptions.ResourceNotFoundException;
import com.moneyfi.user.exceptions.ScenarioNotPossibleException;
import com.moneyfi.user.model.auth.OtpTempModel;
import com.moneyfi.user.model.auth.UserAuthModel;
import com.moneyfi.user.repository.general.ProfileRepository;
import com.moneyfi.user.repository.auth.UserRepository;
import com.moneyfi.user.repository.gmailsync.GmailSyncRepository;
import com.moneyfi.user.service.user.dto.request.AccountBlockOrDeleteRequestDto;
import com.moneyfi.user.service.user.dto.request.ChangePasswordDto;
import com.moneyfi.user.service.user.dto.request.GmailSyncCountIncreaseRequestDto;
import com.moneyfi.user.service.maintainer.dto.request.CreateOrUpdateAdminRequestDto;
import com.moneyfi.user.util.constants.StringConstants;
import com.moneyfi.user.util.enums.AccDeactivationType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.LocalDateTime;

import static com.moneyfi.user.util.constants.StringConstants.*;

@Slf4j
public class UserValidations {

    private UserValidations() {}

    private static final String INPUT_FIELDS_SHOULD_NOT_EMPTY_MESSAGE = "Input fields should not be empty";
    private static final String USER_UNABLE_OPERATIONS = "User is not active to perform the operation";
    private static final String PLEASE_ENTER_CORRECT_OTP = "Please enter correct otp";
    private static final String OTP_EXPIRED_MESSAGE = "Otp expired, Try new one";
    private static final String INCORRECT_PASSWORD_MESSAGE = "Incorrect Password entered!";
    private static final String DESCRIPTION_MANDATORY_MESSAGE = "Description is mandatory";
    private static final String PASSWORD_CHANGE_LIMIT_CROSSED_MESSAGE = "Your Password change limit reached for today, Try tomorrow";

    public static void checkPhoneNumberValidations(String phone) {
        if (!phone.matches("\\d+")) {
            throw new ScenarioNotPossibleException(StringConstants.PHONE_NUMBER_DIGITS_ONLY_MESSAGE);
        }
        if (phone.length() != 10) {
            throw new ScenarioNotPossibleException(StringConstants.PHONE_NUMBER_MAX_LENGTH_MESSAGE);
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
            if (userPassword == null || userPassword.isEmpty() || !StringConstants.encoder.matches(userPassword, user.getPassword())) {
                throw new ScenarioNotPossibleException(INCORRECT_PASSWORD_MESSAGE);
            }
        }
    }

    public static void validateUserGmailSyncCountIncreaseRequest(GmailSyncCountIncreaseRequestDto request, Long userId, GmailSyncRepository gmailSyncRepository) {
        if (request.getCount() <= 0 || request.getCount() > 3) {
            throw new ScenarioNotPossibleException("Please enter valid count between 0 to 3");
        }
        if (ObjectUtils.isEmpty(request.getReason())) {
            throw new ScenarioNotPossibleException("Reason should not be empty");
        }
        Integer currentCount = gmailSyncRepository.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException(GMAIL_AUTH_NOT_FOUND)).getCount();
        log.info("checking count: {}", currentCount);
        if (ObjectUtils.isEmpty(currentCount)) {
            throw new ScenarioNotPossibleException("Gmail consent details not found");
        } else {
            if (currentCount < 3) {
                throw new ScenarioNotPossibleException("You still have " + (3 - currentCount) + " chance/chances");
            }
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
}
