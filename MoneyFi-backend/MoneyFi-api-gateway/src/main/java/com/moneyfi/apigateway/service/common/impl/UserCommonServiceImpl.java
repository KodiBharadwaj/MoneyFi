package com.moneyfi.apigateway.service.common.impl;

import com.moneyfi.apigateway.exceptions.ResourceNotFoundException;
import com.moneyfi.apigateway.model.auth.BlackListedToken;
import com.moneyfi.apigateway.model.auth.SessionTokenModel;
import com.moneyfi.apigateway.model.auth.UserAuthModel;
import com.moneyfi.apigateway.repository.user.auth.SessionTokenRepository;
import com.moneyfi.apigateway.repository.user.auth.TokenBlackListRepository;
import com.moneyfi.apigateway.repository.user.auth.UserRepository;
import com.moneyfi.apigateway.repository.user.ProfileRepository;
import com.moneyfi.apigateway.service.common.UserCommonService;
import com.moneyfi.apigateway.util.EmailFilter;
import com.moneyfi.apigateway.util.EmailTemplates;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class UserCommonServiceImpl implements UserCommonService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final SessionTokenRepository sessionTokenRepository;
    private final TokenBlackListRepository tokenBlacklistRepository;

    public UserCommonServiceImpl(UserRepository userRepository,
                                 ProfileRepository profileRepository,
                                 SessionTokenRepository sessionTokenRepository,
                                 TokenBlackListRepository tokenBlacklistRepository){
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.sessionTokenRepository = sessionTokenRepository;
        this.tokenBlacklistRepository = tokenBlacklistRepository;
    }


    @Override
    @Transactional
    public String forgotPassword(String email) {

        UserAuthModel userAuthModel = userRepository.findByUsername(email);
        if(userAuthModel == null){
            throw new ResourceNotFoundException("No userAuthModel Found");
        }

        String verificationCode = EmailFilter.generateVerificationCode();


        userAuthModel.setVerificationCode(verificationCode);
        userAuthModel.setVerificationCodeExpiration(LocalDateTime.now().plusMinutes(5));
        userAuthModel.setOtpCount(userAuthModel.getOtpCount() + 1);
        userRepository.save(userAuthModel);

        String userName = profileRepository.findByUserId(userAuthModel.getId()).getName();

        boolean isMailSent = EmailTemplates.sendOtpForForgotPassword(userName, email, verificationCode);
        if(isMailSent){
            return "Verification code sent to your email!";
        }

        return "cant send mail!";
    }

    @Override
    public boolean verifyCode(String email, String code) {
        UserAuthModel userAuthModel = userRepository.findByUsername(email);
        if(userAuthModel == null){
             throw new ResourceNotFoundException("UserAuthModel not found");
        }

        return userAuthModel.getVerificationCode().equals(code) && LocalDateTime.now().isBefore(userAuthModel.getVerificationCodeExpiration());
    }

    @Override
    public String UpdatePassword(String email,String password){
        UserAuthModel userAuthModel = userRepository.findByUsername(email);
        if(userAuthModel ==null){
            return "userAuthModel not found for given email...";
        }

        PasswordEncoder passwordEncoder=new BCryptPasswordEncoder();
        userAuthModel.setPassword(passwordEncoder.encode(password));
        userRepository.save(userAuthModel);
        return "Password updated successfully!...";
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

    @Scheduled(fixedRate = 3600000) // Runs every 1 hour
    @Transactional
    public void removeExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Checking for expired tokens at: " + now);
        tokenBlacklistRepository.deleteByExpiryBefore(now);  // Deletes expired tokens
    }
}
