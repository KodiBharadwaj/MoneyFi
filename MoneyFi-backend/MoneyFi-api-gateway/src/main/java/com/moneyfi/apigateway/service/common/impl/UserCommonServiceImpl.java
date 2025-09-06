package com.moneyfi.apigateway.service.common.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moneyfi.apigateway.exceptions.ScenarioNotPossibleException;
import com.moneyfi.apigateway.exceptions.ResourceNotFoundException;
import com.moneyfi.apigateway.model.auth.BlackListedToken;
import com.moneyfi.apigateway.model.auth.SessionTokenModel;
import com.moneyfi.apigateway.model.auth.UserAuthModel;
import com.moneyfi.apigateway.model.common.ContactUs;
import com.moneyfi.apigateway.model.common.ContactUsHist;
import com.moneyfi.apigateway.model.common.ProfileModel;
import com.moneyfi.apigateway.model.common.UserNotification;
import com.moneyfi.apigateway.repository.common.CommonServiceRepository;
import com.moneyfi.apigateway.repository.user.ContactUsHistRepository;
import com.moneyfi.apigateway.repository.user.ContactUsRepository;
import com.moneyfi.apigateway.repository.user.UserNotificationRepository;
import com.moneyfi.apigateway.repository.user.auth.SessionTokenRepository;
import com.moneyfi.apigateway.repository.user.auth.TokenBlackListRepository;
import com.moneyfi.apigateway.repository.user.auth.UserRepository;
import com.moneyfi.apigateway.repository.user.ProfileRepository;
import com.moneyfi.apigateway.service.common.UserCommonService;
import com.moneyfi.apigateway.service.common.dto.request.AccountRetrieveRequestDto;
import com.moneyfi.apigateway.service.common.dto.request.NameChangeRequestDto;
import com.moneyfi.apigateway.service.common.dto.response.QuoteResponseDto;
import com.moneyfi.apigateway.service.common.dto.response.UserNotificationResponseDto;
import com.moneyfi.apigateway.service.common.dto.response.UserRequestStatusDto;
import com.moneyfi.apigateway.util.EmailTemplates;
import com.moneyfi.apigateway.util.constants.StringUtils;
import com.moneyfi.apigateway.util.enums.RaiseRequestStatus;
import com.moneyfi.apigateway.util.enums.RequestReason;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

import static com.moneyfi.apigateway.util.constants.StringUtils.DAILY_QUOTE_EXTERNAL_API_URL;
import static com.moneyfi.apigateway.util.constants.StringUtils.generateVerificationCode;

@Service
@Slf4j
public class UserCommonServiceImpl implements UserCommonService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final SessionTokenRepository sessionTokenRepository;
    private final TokenBlackListRepository tokenBlacklistRepository;
    private final ContactUsRepository contactUsRepository;
    private final ContactUsHistRepository contactUsHistRepository;
    private final CommonServiceRepository commonServiceRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final EmailTemplates emailTemplates;
    private final RestTemplate externalRestTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public UserCommonServiceImpl(UserRepository userRepository,
                                 ProfileRepository profileRepository,
                                 SessionTokenRepository sessionTokenRepository,
                                 TokenBlackListRepository tokenBlacklistRepository,
                                 ContactUsRepository contactUsRepository,
                                 ContactUsHistRepository contactUsHistRepository,
                                 CommonServiceRepository commonServiceRepository,
                                 UserNotificationRepository userNotificationRepository,
                                 EmailTemplates emailTemplates,
                                 RestTemplate externalRestTemplate){
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.sessionTokenRepository = sessionTokenRepository;
        this.tokenBlacklistRepository = tokenBlacklistRepository;
        this.contactUsRepository = contactUsRepository;
        this.contactUsHistRepository = contactUsHistRepository;
        this.commonServiceRepository = commonServiceRepository;
        this.userNotificationRepository = userNotificationRepository;
        this.emailTemplates = emailTemplates;
        this.externalRestTemplate = externalRestTemplate;
    }

    private static final String REFERENCE_NUMBER_SENT = "Reference already sent, Please submit your details";
    private static final String USER_NOT_FOUND = "User not found. Please check your details";
    private static final String DETAILS_ALREADY_SUBMITTED = "Details are already submitted. Please check the status";


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

        boolean isMailSent = emailTemplates.sendOtpForForgotPassword(userName, email, verificationCode);
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
                throw new ScenarioNotPossibleException(contactUsHistRepository.findByContactUsId(report.get().getId()).size()==1?REFERENCE_NUMBER_SENT :
                        DETAILS_ALREADY_SUBMITTED);
            }

            Optional<ContactUs> requestDetails = contactUsDetails
                    .stream()
                    .filter(ContactUs::isRequestActive)
                    .filter(i -> i.getRequestReason().equalsIgnoreCase(RequestReason.ACCOUNT_BLOCK_REQUEST.name()))
                    .findFirst();

            boolean isEmailSent = emailTemplates
                    .sendReferenceNumberEmail(profileRepository.findByUserId(user.getId()).getName(), email, "account unblock", requestDetails.get().getReferenceNumber());

            if(isEmailSent){
                requestDetails.get().setRequestStatus(RaiseRequestStatus.INITIATED.name());
                requestDetails.get().setRequestReason(RequestReason.ACCOUNT_UNBLOCK_REQUEST.name());
                ContactUs savedRequest = contactUsRepository.save(requestDetails.get());

                ContactUsHist requestDetailsHist = new ContactUsHist();
                requestDetailsHist.setContactUsId(savedRequest.getId());
                requestDetailsHist.setMessage("Reference number requested to unblock the account");
                requestDetailsHist.setRequestStatus(RaiseRequestStatus.INITIATED.name());
                requestDetailsHist.setRequestReason(RequestReason.ACCOUNT_UNBLOCK_REQUEST.name());
                requestDetailsHist.setUpdatedTime(LocalDateTime.now());
                contactUsHistRepository.save(requestDetailsHist);
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
                throw new ScenarioNotPossibleException(contactUsHistRepository.findByContactUsId(report.get().getId()).size()==1?REFERENCE_NUMBER_SENT :
                        DETAILS_ALREADY_SUBMITTED);
            }

            ProfileModel userProfile = profileRepository.findByUserId(user.getId());
            String referenceNumber = "NA" + userProfile.getName().substring(0,2) + email.substring(0,2)
                    + (userProfile.getPhone() != null ? userProfile.getPhone().substring(0,2) + generateVerificationCode().substring(0,3) : generateVerificationCode());

            boolean isEmailSent = emailTemplates
                    .sendReferenceNumberEmail(profileRepository.findByUserId(user.getId()).getName(), email, "change name", referenceNumber);

            if(isEmailSent){
                ContactUs saveRequest = new ContactUs();
                saveRequest.setEmail(email);
                saveRequest.setReferenceNumber(referenceNumber);
                saveRequest.setRequestActive(true);
                saveRequest.setVerified(false);
                saveRequest.setRequestStatus(RaiseRequestStatus.INITIATED.name());
                saveRequest.setRequestReason(RequestReason.NAME_CHANGE_REQUEST.name());
                saveRequest.setStartTime(LocalDateTime.now());
                ContactUs savedRequest = contactUsRepository.save(saveRequest);

                ContactUsHist userRequestHist = new ContactUsHist();
                userRequestHist.setContactUsId(savedRequest.getId());
                userRequestHist.setRequestStatus(RaiseRequestStatus.INITIATED.name());
                userRequestHist.setRequestReason(RequestReason.NAME_CHANGE_REQUEST.name());
                userRequestHist.setMessage("Request Reference to change the name");
                userRequestHist.setUpdatedTime(savedRequest.getStartTime());
                contactUsHistRepository.save(userRequestHist);
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
                throw new ScenarioNotPossibleException(contactUsHistRepository.findByContactUsId(report.get().getId()).size()==1?REFERENCE_NUMBER_SENT :
                        DETAILS_ALREADY_SUBMITTED);
            }

            String referenceNumber = StringUtils.generateAlphabetCode() + generateVerificationCode();
            boolean isEmailSent = emailTemplates
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

            ContactUs user = report.orElseThrow(() -> new RuntimeException());

            if(user.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.INITIATED.name())){
                if(!user.getReferenceNumber().equals(requestDto.getReferenceNumber()))
                    throw new ScenarioNotPossibleException("Incorrect Reference Number!");

                user.setRequestStatus(RaiseRequestStatus.SUBMITTED.name());
                ContactUs savedRequest = contactUsRepository.save(user);

                ContactUsHist userRequestHist = new ContactUsHist();
                userRequestHist.setContactUsId(savedRequest.getId());
                userRequestHist.setName(requestDto.getName());
                userRequestHist.setMessage(requestDto.getDescription());
                userRequestHist.setUpdatedTime(LocalDateTime.now());
                userRequestHist.setRequestStatus(RaiseRequestStatus.SUBMITTED.name());
                userRequestHist.setRequestReason(RequestReason.ACCOUNT_UNBLOCK_REQUEST.name());
                contactUsHistRepository.save(userRequestHist);
            } else if(user.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.SUBMITTED.name())){
                throw new ScenarioNotPossibleException("Request already raised");
            }
        } else if (requestDto.getRequestReason().equalsIgnoreCase(RequestReason.ACCOUNT_NOT_DELETE_REQUEST.name())){
            Optional<ContactUs> report = contactUsDetails
                    .stream()
                    .filter(ContactUs::isRequestActive)
                    .filter(i -> i.getRequestReason().equalsIgnoreCase(RequestReason.ACCOUNT_NOT_DELETE_REQUEST.name()))
                    .findFirst();
            ContactUs user = report.orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));

            if(!user.getReferenceNumber().equals(requestDto.getReferenceNumber()))
                throw new ScenarioNotPossibleException("Incorrect Reference Number!");
            user.setRequestStatus(RaiseRequestStatus.SUBMITTED.name());
            ContactUs savedRequest = contactUsRepository.save(user);

            ContactUsHist userRequestHist = new ContactUsHist();
            userRequestHist.setContactUsId(savedRequest.getId());
            userRequestHist.setName(requestDto.getName());
            userRequestHist.setMessage(requestDto.getDescription());
            userRequestHist.setUpdatedTime(LocalDateTime.now());
            contactUsHistRepository.save(userRequestHist);
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

        ContactUs user = report.orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));

        if(!user.getReferenceNumber().equals(requestDto.getReferenceNumber()))
            throw new ScenarioNotPossibleException("Incorrect Reference Number!");

        user.setRequestStatus(RaiseRequestStatus.SUBMITTED.name());
        ContactUs savedRequest = contactUsRepository.save(user);

        ContactUsHist userRequestHist = new ContactUsHist();
        userRequestHist.setContactUsId(savedRequest.getId());
        userRequestHist.setName(requestDto.getNewName());
        userRequestHist.setMessage(requestDto.getOldName());
        userRequestHist.setUpdatedTime(LocalDateTime.now());
        userRequestHist.setRequestReason(RequestReason.NAME_CHANGE_REQUEST.name());
        userRequestHist.setRequestStatus(RaiseRequestStatus.SUBMITTED.name());
        contactUsHistRepository.save(userRequestHist);
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
        } else if(userRequestResponse.getRequestType().equalsIgnoreCase(RequestReason.USER_DEFECT_UPDATE.name())){
            userRequestResponse.setRequestType("Issue raised");
        } else if(userRequestResponse.getRequestType().equalsIgnoreCase(RequestReason.ACCOUNT_BLOCK_REQUEST.name())){
            userRequestResponse.setRequestType("Request Raised to block account");
        }

        if(userRequestResponse.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.INITIATED.name())){
            userRequestResponse.setRequestStatus("User yet to submit the details - open");
        } else if (userRequestResponse.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.SUBMITTED.name())){
            if(userRequestResponse.getRequestType().equalsIgnoreCase("Request Raised to block account"))
                userRequestResponse.setRequestStatus("Account blocked, Raise request to unblock");
            else userRequestResponse.setRequestStatus("Admin approval is in progress");
        } else if(userRequestResponse.getRequestStatus().equalsIgnoreCase(RaiseRequestStatus.COMPLETED.name())){
            userRequestResponse.setRequestStatus("Request has been approved by admin");
        }

        return userRequestResponse;
    }

    @Override
    public QuoteResponseDto getTodayQuoteByExternalCall(String externalApiUrl) {
        QuoteResponseDto quoteResponseDto = new QuoteResponseDto();

        try {
            String jsonStringResponse = externalRestTemplate.getForObject(DAILY_QUOTE_EXTERNAL_API_URL, String.class);

            List<QuoteResponseDto> quoteList = objectMapper.readValue(jsonStringResponse, new TypeReference<List<QuoteResponseDto>>() {});
            if(!quoteList.isEmpty()){
                quoteResponseDto.setQuote(quoteList.get(0).getQuote());
                quoteResponseDto.setAuthor(quoteList.get(0).getAuthor());
                quoteResponseDto.setDescription(quoteList.get(0).getDescription());
                return quoteResponseDto;
            }

        } catch (Exception e){
            e.printStackTrace();
            throw new ScenarioNotPossibleException("Failed to parse the json response -> " + e);
        }

        throw new ResourceNotFoundException("No quote response found from external api");
    }

    @Override
    public List<UserNotificationResponseDto> getUserNotifications(String username) {
        return commonServiceRepository.getUserNotifications(username);
    }

    @Override
    public Integer getUserNotificationsCount(String username) {
        return Math.toIntExact(getUserNotifications(username)
                .stream()
                .filter(notification -> !notification.isRead())
                .count());
    }

    @Override
    @Transactional
    public void updateUserNotificationSeenStatus(String username, String notificationIds) {
        List<Long> ids = Arrays.stream(notificationIds.split(","))
                .map(String::trim)
                .map(Long::parseLong)
                .toList();
        ids.forEach(id -> {
            UserNotification notification = userNotificationRepository.findByScheduleIdAndUsername(id, username);
            if(notification == null){
                throw new ResourceNotFoundException("Notification details are not found");
            }
            notification.setRead(true);
            userNotificationRepository.save(notification);
        });
    }
}
