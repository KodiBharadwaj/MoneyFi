package com.moneyfi.apigateway.service.common.impl;

import com.moneyfi.apigateway.exceptions.ScenarioNotPossibleException;
import com.moneyfi.apigateway.exceptions.ResourceNotFoundException;
import com.moneyfi.apigateway.model.auth.BlackListedToken;
import com.moneyfi.apigateway.model.auth.SessionTokenModel;
import com.moneyfi.apigateway.model.auth.UserAuthModel;
import com.moneyfi.apigateway.model.common.ContactUs;
import com.moneyfi.apigateway.repository.user.ContactUsRepository;
import com.moneyfi.apigateway.repository.user.auth.SessionTokenRepository;
import com.moneyfi.apigateway.repository.user.auth.TokenBlackListRepository;
import com.moneyfi.apigateway.repository.user.auth.UserRepository;
import com.moneyfi.apigateway.repository.user.ProfileRepository;
import com.moneyfi.apigateway.service.common.UserCommonService;
import com.moneyfi.apigateway.service.common.dto.request.AccountRetrieveRequestDto;
import com.moneyfi.apigateway.service.common.dto.request.NameChangeRequestDto;
import com.moneyfi.apigateway.util.EmailTemplates;
import com.moneyfi.apigateway.util.enums.RequestReason;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.moneyfi.apigateway.util.EmailFilter.generateAlphabetCode;
import static com.moneyfi.apigateway.util.EmailFilter.generateVerificationCode;

@Service
@Slf4j
public class UserCommonServiceImpl implements UserCommonService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final SessionTokenRepository sessionTokenRepository;
    private final TokenBlackListRepository tokenBlacklistRepository;
    private final ContactUsRepository contactUsRepository;

    public UserCommonServiceImpl(UserRepository userRepository,
                                 ProfileRepository profileRepository,
                                 SessionTokenRepository sessionTokenRepository,
                                 TokenBlackListRepository tokenBlacklistRepository,
                                 ContactUsRepository contactUsRepository){
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.sessionTokenRepository = sessionTokenRepository;
        this.tokenBlacklistRepository = tokenBlacklistRepository;
        this.contactUsRepository = contactUsRepository;
    }


    @Override
    @Transactional
    public String forgotPassword(String email) {

        UserAuthModel userAuthModel = userRepository.getUserDetailsByUsername(email);
        if(userAuthModel == null){
            throw new ResourceNotFoundException("No userAuthModel Found");
        }
        else if(userAuthModel.isBlocked()){
            return "Account Blocked! Please contact admin";
        }
        else if(userAuthModel.isDeleted()){
            return "Account Deleted! Please contact admin";
        }

        String verificationCode = generateVerificationCode();


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
        UserAuthModel userAuthModel = userRepository.getUserDetailsByUsername(email);
        if(userAuthModel == null){
             throw new ResourceNotFoundException("UserAuthModel not found");
        }

        return userAuthModel.getVerificationCode().equals(code) && LocalDateTime.now().isBefore(userAuthModel.getVerificationCodeExpiration());
    }

    @Override
    public String updatePassword(String email, String password){
        UserAuthModel userAuthModel = userRepository.getUserDetailsByUsername(email);
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

    @Override
    public Map<Boolean, String> sendReferenceRequestNumberEmail(String requestStatus, String email) {
        Map<Boolean, String> response = new HashMap<>();

        UserAuthModel user = userRepository.getUserDetailsByUsername(email);
        if(user == null){
            throw new ResourceNotFoundException("User not found");
        }

        List<ContactUs> contactUsDetails = contactUsRepository.findByEmail(email);
        String referenceNumber = generateAlphabetCode() + generateVerificationCode();

        if(requestStatus.equalsIgnoreCase(RequestReason.ACCOUNT_UNBLOCK_REQUEST.name())){
            if(!user.isBlocked()){
                throw new ScenarioNotPossibleException("User is not blocked to perform this operation");
            }

            Optional<ContactUs> report = contactUsDetails
                    .stream()
                    .filter(ContactUs::isRequestActive)
                    .filter(i -> i.getRequestReason().equalsIgnoreCase(RequestReason.ACCOUNT_UNBLOCK_REQUEST.name()))
                    .findFirst();
            if(report.isPresent()){
                throw new ScenarioNotPossibleException("Account unblock request is already raised");
            }

            boolean isEmailSent = EmailTemplates
                    .sendReferenceNumberEmail(profileRepository.findByUserId(user.getId()).getName(), email, "account unblock", referenceNumber);

            if(isEmailSent){
                response.put(true, "Reference Number sent");
                return response;
            }
        } else if (requestStatus.equalsIgnoreCase(RequestReason.NAME_CHANGE_REQUEST.name())){
            if(user.isBlocked()){
                throw new ScenarioNotPossibleException("Name change is not possible since user is blocked");
            }

            Optional<ContactUs> report = contactUsDetails
                    .stream()
                    .filter(ContactUs::isRequestActive)
                    .filter(i -> i.getRequestReason().equalsIgnoreCase(RequestReason.NAME_CHANGE_REQUEST.name()))
                    .findFirst();
            if(report.isPresent()){
                throw new ScenarioNotPossibleException("Name change request is already raised");
            }

            boolean isEmailSent = EmailTemplates
                    .sendReferenceNumberEmail(profileRepository.findByUserId(user.getId()).getName(), email, "change name", referenceNumber);

            if(isEmailSent){
                response.put(true, "Reference Number sent to your email");
                return response;
            }
        }

        response.put(false, "Failed to send email! Try later");
        return response;
    }

    @Override
    @Transactional
    public void accountUnblockRequestByUser(AccountRetrieveRequestDto requestDto) {
        ContactUs contactUs = new ContactUs();
        contactUs.setName(requestDto.getName());
        contactUs.setMessage(requestDto.getDescription());
        contactUs.setEmail(requestDto.getUsername());
        contactUs.setReferenceNumber(requestDto.getReferenceNumber());
        contactUs.setRequestActive(true);
        contactUs.setVerified(false);
        contactUs.setRequestReason(RequestReason.ACCOUNT_UNBLOCK_REQUEST.name());
        contactUsRepository.save(contactUs);
    }

    @Override
    @Transactional
    public void nameChangeRequestByUser(NameChangeRequestDto requestDto) {
        ContactUs contactUs = new ContactUs();
        contactUs.setName(requestDto.getNewName());
        contactUs.setMessage(requestDto.getOldName());
        contactUs.setEmail(requestDto.getEmail());
        contactUs.setReferenceNumber(requestDto.getReferenceNumber());
        contactUs.setRequestActive(true);
        contactUs.setVerified(false);
        contactUs.setRequestReason(RequestReason.NAME_CHANGE_REQUEST.name());
        contactUsRepository.save(contactUs);
    }





    @Scheduled(fixedRate = 3600000) // Runs every 1 hour
    @Transactional
    public void removeExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Checking for expired tokens at: " + now);
        tokenBlacklistRepository.deleteByExpiryBefore(now);  // Deletes expired tokens
    }
}
