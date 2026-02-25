package com.moneyfi.apigateway.service.common.impl;

import com.moneyfi.apigateway.exceptions.ScenarioNotPossibleException;
import com.moneyfi.apigateway.exceptions.ResourceNotFoundException;
import com.moneyfi.apigateway.model.auth.BlackListedToken;
import com.moneyfi.apigateway.model.auth.SessionTokenModel;
import com.moneyfi.apigateway.model.auth.UserAuthModel;
import com.moneyfi.apigateway.model.common.*;
import com.moneyfi.apigateway.repository.user.*;
import com.moneyfi.apigateway.repository.user.auth.SessionTokenRepository;
import com.moneyfi.apigateway.repository.user.auth.TokenBlackListRepository;
import com.moneyfi.apigateway.repository.user.auth.UserRepository;
import com.moneyfi.apigateway.service.common.UserCommonService;
import com.moneyfi.apigateway.service.general.dto.NotificationQueueDto;
import com.moneyfi.apigateway.util.enums.NotificationQueueEnum;
import com.moneyfi.apigateway.util.enums.ReasonEnum;
import com.moneyfi.apigateway.validator.UserValidations;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.moneyfi.apigateway.util.constants.StringConstants.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserCommonServiceImpl implements UserCommonService {

    private final UserRepository userRepository;
    private final SessionTokenRepository sessionTokenRepository;
    private final TokenBlackListRepository tokenBlacklistRepository;
    private final UserAuthHistRepository userAuthHistRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional(rollbackOn = Exception.class)
    public String forgotPassword(String email) {
        UserAuthModel user = userRepository.getUserDetailsByUsername(email).orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        UserValidations.userAlreadyDeactivatedCheckValidation(user);
        String verificationCode = null;
        if (user.getVerificationCode() != null && LocalDateTime.now().isBefore(user.getVerificationCodeExpiration())) {
            verificationCode = user.getVerificationCode();
        } else {
            verificationCode = generateVerificationCode();
            user.setVerificationCode(verificationCode);
            user.setVerificationCodeExpiration(LocalDateTime.now().plusMinutes(5));
            user.setOtpCount(user.getOtpCount() + 1);
            userRepository.save(user);
        }
        applicationEventPublisher.publishEvent(new NotificationQueueDto(NotificationQueueEnum.OTP_FOR_FORGOT_PASSWORD.name(), userRepository.getUserNameByUsername(email.trim()) + "<|>" + email + "<|>" + verificationCode));
        return VERIFICATION_CODE_SENT_MESSAGE;
    }

    @Override
    public String verifyCode(String email, String code) {
        UserAuthModel user = userRepository.getUserDetailsByUsername(email).orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        boolean response = user.getVerificationCode().equals(code) && LocalDateTime.now().isBefore(user.getVerificationCodeExpiration());
        if (response) return VERIFICATION_SUCCESSFUL_MESSAGE;
        else throw new ScenarioNotPossibleException(VERIFICATION_FAILURE_MESSAGE);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public String updatePasswordOnUserForgotMode(String email, String password){
        UserAuthModel user = userRepository.getUserDetailsByUsername(email).orElseThrow(()-> new ResourceNotFoundException(USER_NOT_FOUND));
        if(encoder.matches(password, user.getPassword())) {
            throw new ScenarioNotPossibleException(SAME_PASSWORD_NOT_ALLOWED_MESSAGE);
        }
        user.setPassword(encoder.encode(password));
        user.setVerificationCode(null);
        user.setVerificationCodeExpiration(null);
        userRepository.save(user);
        userAuthHistRepository.save(new UserAuthHist(user.getId(), LocalDateTime.now(), reasonCodeIdAssociation.get(ReasonEnum.FORGOT_PASSWORD), PASSWORD_UPDATED_MODE_USING_FORGOT, user.getId()));
        return PASSWORD_UPDATED_SUCCESSFULLY;
    }

    @Override
    public SessionTokenModel save(SessionTokenModel sessionTokenModel) {
        return sessionTokenRepository.save(sessionTokenModel);
    }

    @Override
    public SessionTokenModel getUserByUsername(String username) {
        return sessionTokenRepository.findByUsername(username);
    }

    @Override
    public SessionTokenModel getSessionDetailsByToken(String token) {
        return sessionTokenRepository.getSessionTokenModelByToken(token);
    }

    @CacheEvict(value = "blackListToken",
            key = "#blackListedToken.username + ':' + #blackListedToken.token")
    public BlackListedToken blacklistToken(BlackListedToken blackListedToken) {
        return tokenBlacklistRepository.save(blackListedToken);
    }

    @Cacheable(value = "blackListToken", key = "#username + ':' + #token")
    public boolean isTokenBlacklisted(String token, String username) {
        return !(tokenBlacklistRepository.findByUsernameAndToken(username, token).isEmpty());
    }
}
