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
import com.moneyfi.apigateway.service.general.email.EmailTemplates;
import com.moneyfi.apigateway.util.enums.ReasonEnum;
import com.moneyfi.apigateway.validator.UserValidations;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

import static com.moneyfi.apigateway.util.constants.StringConstants.*;

@Service
@Slf4j
public class UserCommonServiceImpl implements UserCommonService {

    private final UserRepository userRepository;
    private final SessionTokenRepository sessionTokenRepository;
    private final TokenBlackListRepository tokenBlacklistRepository;
    private final EmailTemplates emailTemplates;
    private final UserAuthHistRepository userAuthHistRepository;

    public UserCommonServiceImpl(UserRepository userRepository,
                                 SessionTokenRepository sessionTokenRepository,
                                 TokenBlackListRepository tokenBlacklistRepository,
                                 EmailTemplates emailTemplates,
                                 UserAuthHistRepository userAuthHistRepository){
        this.userRepository = userRepository;
        this.sessionTokenRepository = sessionTokenRepository;
        this.tokenBlacklistRepository = tokenBlacklistRepository;
        this.emailTemplates = emailTemplates;
        this.userAuthHistRepository = userAuthHistRepository;
    }

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
        emailTemplates.sendOtpForForgotPassword(userRepository.getUserNameByUsername(email.trim()), email, verificationCode);
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

    public BlackListedToken blacklistToken(BlackListedToken blackListedToken) {
        tokenBlacklistRepository.save(blackListedToken);
        return blackListedToken;
    }

    public boolean isTokenBlacklisted(String token) {
        List<BlackListedToken> blackListedTokens = tokenBlacklistRepository.findByToken(token);
        return !(blackListedTokens.isEmpty());
    }
}
