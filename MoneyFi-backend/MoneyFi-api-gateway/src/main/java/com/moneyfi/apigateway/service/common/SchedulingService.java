package com.moneyfi.apigateway.service.common;

import com.moneyfi.apigateway.dto.ContactUs;
import com.moneyfi.apigateway.dto.ContactUsHist;
import com.moneyfi.apigateway.dto.ProfileModel;
import com.moneyfi.apigateway.model.auth.OtpTempModel;
import com.moneyfi.apigateway.model.auth.SessionTokenModel;
import com.moneyfi.apigateway.model.auth.UserAuthModel;
import com.moneyfi.apigateway.model.common.*;
import com.moneyfi.apigateway.model.gmailsync.GmailAuth;
import com.moneyfi.apigateway.repository.common.CommonServiceRepository;
import com.moneyfi.apigateway.repository.gmailsync.GmailSyncRepository;
import com.moneyfi.apigateway.repository.user.*;
import com.moneyfi.apigateway.repository.user.auth.OtpTempRepository;
import com.moneyfi.apigateway.repository.user.auth.SessionTokenRepository;
import com.moneyfi.apigateway.repository.user.auth.TokenBlackListRepository;
import com.moneyfi.apigateway.repository.user.auth.UserRepository;
import com.moneyfi.apigateway.util.EmailTemplates;
import com.moneyfi.apigateway.util.constants.StringConstants;
import com.moneyfi.apigateway.util.enums.ReasonEnum;
import com.moneyfi.apigateway.util.enums.UserRoles;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.moneyfi.apigateway.util.constants.StringConstants.CURRENT_DATE_TIME;
import static com.moneyfi.apigateway.util.constants.StringConstants.userRoleAssociation;

@Service
@Slf4j
public class SchedulingService {

    private final TokenBlackListRepository tokenBlacklistRepository;
    private final UserRepository userRepository;
    private final CommonServiceRepository commonServiceRepository;
    private final EmailTemplates emailTemplates;
    private final UserAuthHistRepository userAuthHistRepository;
    private final SessionTokenRepository sessionTokenRepository;
    private final OtpTempRepository otpTempRepository;
    private final GmailSyncRepository gmailSyncRepository;

    public SchedulingService(TokenBlackListRepository tokenBlacklistRepository,
                             UserRepository userRepository,
                             CommonServiceRepository commonServiceRepository,
                             EmailTemplates emailTemplates,
                             UserAuthHistRepository userAuthHistRepository,
                             SessionTokenRepository sessionTokenRepository,
                             OtpTempRepository otpTempRepository,
                             GmailSyncRepository gmailSyncRepository){
        this.tokenBlacklistRepository = tokenBlacklistRepository;
        this.userRepository = userRepository;
        this.commonServiceRepository = commonServiceRepository;
        this.emailTemplates = emailTemplates;
        this.userAuthHistRepository = userAuthHistRepository;
        this.sessionTokenRepository = sessionTokenRepository;
        this.otpTempRepository = otpTempRepository;
        this.gmailSyncRepository = gmailSyncRepository;
    }

    @PostConstruct
    public void initializeScheduledMethodsInCaseOfServiceRunningDelay(){
        dailyJobRunInBeginningOfTheDay();
        runOnFirstDayOfMonthAtMidnightForRecurringIncomeAndExpense();
    }

    @Scheduled(fixedRate = 3600000) // Runs every 1 hour
    @Transactional
    public void removeExpiredTokens() {
        /** Scheduling algorithm to delete the expired tokens in the table **/
        tokenBlacklistRepository.deleteByExpiryBefore(CURRENT_DATE_TIME);
    }

    @Scheduled(cron = "0 0 0 * * *") // Runs at every 12 am of the day (starting of the day)
    public void dailyJobRunInBeginningOfTheDay(){
        /** Scheduling algorithm to remove the previous day otp count which are greater than three for user auth table for otp sending **/
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        List<UserAuthModel> userAuthModelList = userRepository.getUserListWhoseOtpCountGreaterThanThree(startOfToday);
        List<UserAuthModel> listToUpdate = new ArrayList<>();
        for (UserAuthModel userAuthModel : userAuthModelList) {
            userAuthModel.setOtpCount(0);
            listToUpdate.add(userAuthModel);
        }
        userRepository.saveAll(listToUpdate);

        /** Scheduling algorithm to remove the previous day otp count which are greater than three in gmail auth table **/
        List<GmailAuth> gmailAuthListToBeUpdated = new ArrayList<>();
        List<GmailAuth> gmailAuthList = gmailSyncRepository.getTransactionsListWhoseCountIsGreaterThanThree();
        for(GmailAuth gmailAuth : gmailAuthList) {
            gmailAuth.setCount(0);
            gmailAuth.setIsActive(Boolean.FALSE);
            gmailAuthListToBeUpdated.add(gmailAuth);
        }
        gmailSyncRepository.saveAll(gmailAuthListToBeUpdated);

        /** Scheduling algorithm to find the users who completed more than 1 year in MoneyFi **/
        List<String> anniversaryUsersList = commonServiceRepository.getBirthdayAndAnniversaryUsersList(LocalDate.now().getMonthValue(), LocalDate.now().getDayOfMonth(), "Birthday");
        new Thread(() -> anniversaryUsersList.forEach(user -> {
            String[] parts = user.split("-");
            int numberOfYears = LocalDate.now().getYear() - Integer.parseInt(parts[2]);
            if(numberOfYears != 0){
                emailTemplates.sendAnniversaryCongratulationsMailToUser(parts[0].trim(), parts[1], numberOfYears);
            }
        })).start();

        /** Scheduling algorithm to find the birthday users **/
        List<String> birthdayList = commonServiceRepository.getBirthdayAndAnniversaryUsersList(LocalDate.now().getMonthValue(), LocalDate.now().getDayOfMonth(), "Anniversary");
        new Thread(() -> birthdayList.forEach(user -> {
            String[] parts = user.split("-");
            int numberOfYears = LocalDate.now().getYear() - Integer.parseInt(parts[2]);
            if(numberOfYears > 0){
                emailTemplates.sendBirthdayWishEmailToUsers(parts[0].trim(), parts[1]);
            }
        })).start();

        /** Scheduling algorithm to delete the users who deleted their account 30 days before **/
        int roleId = 0;
        for (Map.Entry<Integer, String> it : userRoleAssociation.entrySet()) {
            if (it.getValue().equalsIgnoreCase(UserRoles.USER.name())) {
                roleId = it.getKey();
            }
        }
        List<UserAuthModel> accountDeletedUsersList = userRepository.getDeletedUsersList(roleId, StringConstants.reasonCodeIdAssociation.get(ReasonEnum.DELETE_ACCOUNT));
        List<UserAuthHist> userAuthHistList = new ArrayList<>();
        List<ContactUs> contactUsList = new ArrayList<>();
        List<ContactUsHist> contactUsHistList = new ArrayList<>();
        List<ProfileModel> userProfileDetailsList = new ArrayList<>();
        List<SessionTokenModel> sessionTokenModelList = new ArrayList<>();
        List<OtpTempModel> otpTempModelList = new ArrayList<>();

        accountDeletedUsersList.forEach(user -> {
            userAuthHistList.addAll(userAuthHistRepository.findByUserId(user.getId()));
            contactUsList.addAll(userRepository.getContactUsRecordsByUsername(user.getUsername()).stream().map(StringConstants::convertContactUsInterfaceToDto).toList());
            userProfileDetailsList.add(userRepository.getUserProfileDetailsByUserId(user.getId()).stream().map(StringConstants::convertProfileDetailsInterfaceToDto).findFirst().get());
            sessionTokenModelList.add(sessionTokenRepository.findByUsername(user.getUsername()));
            otpTempModelList.addAll(otpTempRepository.findByEmail(user.getUsername()));
        });
        contactUsList.forEach(contactUs ->
            contactUsHistList.addAll(userRepository.getContactUsHistoryDetailsByContactUsId(contactUs.getId()).stream().map(StringConstants::convertContactUsHistInterfaceToDto).toList()));
        userRepository.deleteAll(accountDeletedUsersList);
        userAuthHistRepository.deleteAll(userAuthHistList);
//        contactUsRepository.deleteAll(contactUsList);
//        contactUsHistRepository.deleteAll(contactUsHistList);
//        profileRepository.deleteAll(userProfileDetailsList);
        sessionTokenRepository.deleteAll(sessionTokenModelList);
        otpTempRepository.deleteAll(otpTempModelList);
        userRepository.deleteIncomeExpenseBudgetGoalsOfDeletedUsers(accountDeletedUsersList.stream().map(UserAuthModel::getId).toList());
    }

    @Scheduled(cron = "0 0 0 1 * *") //Runs on 1st of every month
    public void runOnFirstDayOfMonthAtMidnightForRecurringIncomeAndExpense() {
        userRepository.updateRecurringIncomesAndExpenses();
    }
}
