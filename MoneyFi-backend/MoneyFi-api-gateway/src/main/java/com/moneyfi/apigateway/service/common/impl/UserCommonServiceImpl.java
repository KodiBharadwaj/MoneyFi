package com.moneyfi.apigateway.service.common.impl;

import com.moneyfi.apigateway.exceptions.ScenarioNotPossibleException;
import com.moneyfi.apigateway.exceptions.ResourceNotFoundException;
import com.moneyfi.apigateway.model.auth.BlackListedToken;
import com.moneyfi.apigateway.model.auth.SessionTokenModel;
import com.moneyfi.apigateway.model.auth.UserAuthModel;
import com.moneyfi.apigateway.model.common.ContactUs;
import com.moneyfi.apigateway.repository.common.CommonServiceRepository;
import com.moneyfi.apigateway.repository.user.ContactUsRepository;
import com.moneyfi.apigateway.repository.user.auth.SessionTokenRepository;
import com.moneyfi.apigateway.repository.user.auth.TokenBlackListRepository;
import com.moneyfi.apigateway.repository.user.auth.UserRepository;
import com.moneyfi.apigateway.repository.user.ProfileRepository;
import com.moneyfi.apigateway.service.common.UserCommonService;
import com.moneyfi.apigateway.service.common.dto.request.AccountRetrieveRequestDto;
import com.moneyfi.apigateway.service.common.dto.request.NameChangeRequestDto;
import com.moneyfi.apigateway.service.common.dto.response.UserRequestStatusDto;
import com.moneyfi.apigateway.util.EmailTemplates;
import com.moneyfi.apigateway.util.constants.StringUtils;
import com.moneyfi.apigateway.util.enums.RaiseRequestStatus;
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

import static com.moneyfi.apigateway.util.constants.StringUtils.generateVerificationCode;

@Service
@Slf4j
public class UserCommonServiceImpl implements UserCommonService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final SessionTokenRepository sessionTokenRepository;
    private final TokenBlackListRepository tokenBlacklistRepository;
    private final ContactUsRepository contactUsRepository;
    private final CommonServiceRepository commonServiceRepository;

    public UserCommonServiceImpl(UserRepository userRepository,
                                 ProfileRepository profileRepository,
                                 SessionTokenRepository sessionTokenRepository,
                                 TokenBlackListRepository tokenBlacklistRepository,
                                 ContactUsRepository contactUsRepository,
                                 CommonServiceRepository commonServiceRepository){
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.sessionTokenRepository = sessionTokenRepository;
        this.tokenBlacklistRepository = tokenBlacklistRepository;
        this.contactUsRepository = contactUsRepository;
        this.commonServiceRepository = commonServiceRepository;
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
    @Transactional
    public Map<Boolean, String> sendReferenceRequestNumberEmail(String requestStatus, String email) {
        Map<Boolean, String> response = new HashMap<>();

        UserAuthModel user = userRepository.getUserDetailsByUsername(email);
        if(user == null){
            throw new ResourceNotFoundException("User not found");
        }

        List<ContactUs> contactUsDetails = contactUsRepository.findByEmail(email);
        String referenceNumber = StringUtils.generateAlphabetCode() + generateVerificationCode();

        if(requestStatus.equalsIgnoreCase(RequestReason.ACCOUNT_UNBLOCK_REQUEST.name())){
            if(user.isDeleted() || !user.isBlocked()){
                throw new ScenarioNotPossibleException(user.isDeleted()?"Account is deleted. Raise retrieval request" :
                        "User is not blocked to perform this operation");
            }

            Optional<ContactUs> report = contactUsDetails
                    .stream()
                    .filter(ContactUs::isRequestActive)
                    .filter(i -> i.getRequestReason().equalsIgnoreCase(RequestReason.ACCOUNT_UNBLOCK_REQUEST.name()))
                    .filter(i -> i.getReferenceNumber() != null)
                    .findFirst();

            if(report.isPresent()){
                throw new ScenarioNotPossibleException(report.get().getName()!=null?"Account unblock request is already raised":
                        "Reference already sent, Please submit your details");
            }

            boolean isEmailSent = EmailTemplates
                    .sendReferenceNumberEmail(profileRepository.findByUserId(user.getId()).getName(), email, "account unblock", referenceNumber);

            if(isEmailSent){
                ContactUs saveRequest = new ContactUs();
                saveRequest.setEmail(email);
                saveRequest.setReferenceNumber(referenceNumber);
                saveRequest.setRequestActive(true);
                saveRequest.setVerified(false);
                saveRequest.setRequestStatus(RaiseRequestStatus.INITIATED.name());
                saveRequest.setRequestReason(RequestReason.ACCOUNT_UNBLOCK_REQUEST.name());
                contactUsRepository.save(saveRequest);

                response.put(true, "Reference Number sent to your email");
                return response;
            }
        } else if (requestStatus.equalsIgnoreCase(RequestReason.NAME_CHANGE_REQUEST.name())){
            if(user.isDeleted() || user.isBlocked()){
                throw new ScenarioNotPossibleException(user.isDeleted()?"Account is deleted. Raise retrieval request" :
                        "Name change is not possible since user is blocked");
            }

            Optional<ContactUs> report = contactUsDetails
                    .stream()
                    .filter(ContactUs::isRequestActive)
                    .filter(i -> i.getRequestReason().equalsIgnoreCase(RequestReason.NAME_CHANGE_REQUEST.name()))
                    .filter(i -> i.getReferenceNumber() != null)
                    .findFirst();
            if(report.isPresent()){
                throw new ScenarioNotPossibleException(report.get().getName()!=null?"Name change request is already raised":
                        "Reference already sent, Please submit your details");
            }

            boolean isEmailSent = EmailTemplates
                    .sendReferenceNumberEmail(profileRepository.findByUserId(user.getId()).getName(), email, "change name", referenceNumber);

            if(isEmailSent){
                ContactUs saveRequest = new ContactUs();
                saveRequest.setEmail(email);
                saveRequest.setReferenceNumber(referenceNumber);
                saveRequest.setRequestActive(true);
                saveRequest.setVerified(false);
                saveRequest.setRequestStatus(RaiseRequestStatus.INITIATED.name());
                saveRequest.setRequestReason(RequestReason.NAME_CHANGE_REQUEST.name());
                contactUsRepository.save(saveRequest);

                response.put(true, "Reference Number sent to your email");
                return response;
            }
        } else if (requestStatus.equalsIgnoreCase(RequestReason.ACCOUNT_NOT_DELETE_REQUEST.name())){
            if(!user.isDeleted()){
                throw new ScenarioNotPossibleException("Account is already in active!");
            }

            Optional<ContactUs> report = contactUsDetails
                    .stream()
                    .filter(ContactUs::isRequestActive)
                    .filter(i -> i.getRequestReason().equalsIgnoreCase(RequestReason.ACCOUNT_NOT_DELETE_REQUEST.name()))
                    .filter(i -> i.getReferenceNumber() != null)
                    .findFirst();
            if(report.isPresent()){
                throw new ScenarioNotPossibleException(report.get().getName()!=null?"Account retrieval request is already raised":
                        "Reference already sent, Please submit your details");
            }

            boolean isEmailSent = EmailTemplates
                    .sendReferenceNumberEmail(profileRepository.findByUserId(user.getId()).getName(), email, "account retrieve", referenceNumber);

            if(isEmailSent){
                ContactUs saveRequest = new ContactUs();
                saveRequest.setEmail(email);
                saveRequest.setReferenceNumber(referenceNumber);
                saveRequest.setRequestActive(true);
                saveRequest.setVerified(false);
                saveRequest.setRequestStatus(RaiseRequestStatus.INITIATED.name());
                saveRequest.setRequestReason(RequestReason.ACCOUNT_NOT_DELETE_REQUEST.name());
                contactUsRepository.save(saveRequest);
                response.put(true, "Reference Number sent");
                return response;
            }
        }

        response.put(false, "Failed to send email! Try later");
        return response;
    }

    @Override
    @Transactional
    public void accountUnblockRequestByUser(AccountRetrieveRequestDto requestDto) {
        List<ContactUs> contactUsDetails = contactUsRepository.findByEmail(requestDto.getUsername());

        if(requestDto.getRequestReason().equalsIgnoreCase(RequestReason.ACCOUNT_UNBLOCK_REQUEST.name())){
            Optional<ContactUs> report = contactUsDetails
                    .stream()
                    .filter(ContactUs::isRequestActive)
                    .filter(i -> i.getRequestReason().equalsIgnoreCase(RequestReason.ACCOUNT_UNBLOCK_REQUEST.name()))
                    .findFirst();

            ContactUs user = report.orElseThrow(() -> new RuntimeException("User not found. Please check your details"));

            if(!user.getReferenceNumber().equals(requestDto.getReferenceNumber()))
                throw new ScenarioNotPossibleException("Incorrect Reference Number!");

            user.setName(requestDto.getName());
            user.setMessage(requestDto.getDescription());
            user.setRequestStatus(RaiseRequestStatus.SUBMITTED.name());
            contactUsRepository.save(user);

        } else if (requestDto.getRequestReason().equalsIgnoreCase(RequestReason.ACCOUNT_NOT_DELETE_REQUEST.name())){
            Optional<ContactUs> report = contactUsDetails
                    .stream()
                    .filter(ContactUs::isRequestActive)
                    .filter(i -> i.getRequestReason().equalsIgnoreCase(RequestReason.ACCOUNT_NOT_DELETE_REQUEST.name()))
                    .findFirst();
            ContactUs user = report.orElseThrow(() -> new RuntimeException("User not found. Please check your details"));

            if(!user.getReferenceNumber().equals(requestDto.getReferenceNumber()))
                throw new ScenarioNotPossibleException("Incorrect Reference Number!");
            user.setName(requestDto.getName());
            user.setMessage(requestDto.getDescription());
            user.setRequestStatus(RaiseRequestStatus.SUBMITTED.name());
            contactUsRepository.save(user);
        }

    }

    @Override
    @Transactional
    public void nameChangeRequestByUser(NameChangeRequestDto requestDto) {
        List<ContactUs> contactUsDetails = contactUsRepository.findByEmail(requestDto.getEmail());
        Optional<ContactUs> report = contactUsDetails
                .stream()
                .filter(ContactUs::isRequestActive)
                .filter(i -> i.getRequestReason().equalsIgnoreCase(RequestReason.NAME_CHANGE_REQUEST.name()))
                .findFirst();

        ContactUs user = report.orElseThrow(() -> new RuntimeException("User not found. Please check your details"));

        if(!user.getReferenceNumber().equals(requestDto.getReferenceNumber()))
            throw new ScenarioNotPossibleException("Incorrect Reference Number!");

        user.setName(requestDto.getNewName());
        user.setMessage(requestDto.getOldName());
        user.setRequestStatus(RaiseRequestStatus.SUBMITTED.name());
        contactUsRepository.save(user);
    }

    @Override
    public UserRequestStatusDto trackUserRequestUsingReferenceNumber(String referenceNumber) {
        UserRequestStatusDto userRequestResponse = commonServiceRepository.trackUserRequestUsingReferenceNumber(referenceNumber);

        if(userRequestResponse.getRequestType().equalsIgnoreCase(RequestReason.NAME_CHANGE_REQUEST.name())){
            userRequestResponse.setRequestType("Requested for Name change");
        } else if(userRequestResponse.getRequestType().equalsIgnoreCase(RequestReason.ACCOUNT_UNBLOCK_REQUEST.name())){
            userRequestResponse.setRequestType("Requested to unblock the account");
        } else if(userRequestResponse.getRequestType().equalsIgnoreCase(RequestReason.ACCOUNT_NOT_DELETE_REQUEST.name())){
            userRequestResponse.setRequestType("Requested to retrieve the account");
        }

        if(userRequestResponse.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.INITIATED.name())){
            userRequestResponse.setRequestStatus("User yet to submit the details - open");
        } else if (userRequestResponse.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.SUBMITTED.name())){
            userRequestResponse.setRequestStatus("Admin approval is in progress");
        } else if(userRequestResponse.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.COMPLETED.name())){
            userRequestResponse.setRequestStatus("Request has been approved by admin");
        }

        return userRequestResponse;
    }


    @Scheduled(fixedRate = 3600000) // Runs every 1 hour
    @Transactional
    public void removeExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Checking for expired tokens at: " + now);
        tokenBlacklistRepository.deleteByExpiryBefore(now);  // Deletes expired tokens
    }
}
