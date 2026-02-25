package com.moneyfi.apigateway.service.general;

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
import com.moneyfi.apigateway.service.general.dto.NotificationQueueDto;
import com.moneyfi.apigateway.util.constants.StringConstants;
import com.moneyfi.apigateway.util.enums.NotificationQueueEnum;
import com.moneyfi.apigateway.util.enums.ReasonEnum;
import com.moneyfi.apigateway.util.enums.UserRoles;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.moneyfi.apigateway.util.constants.StringConstants.userRoleAssociation;

@Service
@Slf4j
@RequiredArgsConstructor
public class SchedulingService {

    private final TokenBlackListRepository tokenBlacklistRepository;
    private final UserRepository userRepository;
    private final CommonServiceRepository commonServiceRepository;
    private final UserAuthHistRepository userAuthHistRepository;
    private final SessionTokenRepository sessionTokenRepository;
    private final OtpTempRepository otpTempRepository;
    private final GmailSyncRepository gmailSyncRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @PostConstruct
    public void initializeScheduledMethodsInCaseOfServiceRunningDelay(){
        dailyJobRunInBeginningOfTheDay();
        runOnFirstDayOfMonthAtMidnightForRecurringIncomeAndExpense();
    }

    @Scheduled(fixedRate = 3600000) // Runs every 1 hour
    @Transactional
    public void removeExpiredTokens() {
        /** Scheduling algorithm to delete the expired tokens in the table **/
        tokenBlacklistRepository.deleteByExpiryBefore(LocalDateTime.now());
    }

    @Scheduled(cron = "0 0 0 * * *") // Runs at every 12 am of the day (starting of the day)
    public void dailyJobRunInBeginningOfTheDay(){
        /** Scheduling algorithm to remove the previous day otp count which are greater than three for user auth table for otp sending **/
        List<UserAuthModel> userAuthModelList = userRepository.findAll().stream().filter(user -> !user.isBlocked() && !user.isDeleted() && userRoleAssociation.get(user.getRoleId()).equalsIgnoreCase(UserRoles.USER.name())).toList();
        List<UserAuthModel> listToUpdate = new ArrayList<>();
        for (UserAuthModel userAuthModel : userAuthModelList) {
            userAuthModel.setOtpCount(0);
            listToUpdate.add(userAuthModel);
        }
        userRepository.saveAll(listToUpdate);

        /** Scheduling algorithm to remove the previous day otp count which are greater than three in gmail auth table **/
        List<GmailAuth> gmailAuthListToBeUpdated = new ArrayList<>();
        List<GmailAuth> gmailAuthList = gmailSyncRepository.findAll();
        for(GmailAuth gmailAuth : gmailAuthList) {
            gmailAuth.setCount(0);
            gmailAuth.setIsActive(Boolean.FALSE);
            gmailAuthListToBeUpdated.add(gmailAuth);
        }
        gmailSyncRepository.saveAll(gmailAuthListToBeUpdated);

        /** Scheduling algorithm to find the users who completed more than 1 year in MoneyFi **/
        List<String> anniversaryUsersList = commonServiceRepository.getBirthdayAndAnniversaryUsersList(LocalDate.now().getMonthValue(), LocalDate.now().getDayOfMonth(), "Birthday");
        anniversaryUsersList.forEach(user -> {
            String[] parts = user.split("-");
            int numberOfYears = LocalDate.now().getYear() - Integer.parseInt(parts[2]);
            if (numberOfYears != 0) {
                applicationEventPublisher.publishEvent(new NotificationQueueDto(NotificationQueueEnum.USER_ANNIVERSARY_MAIL.name(), parts[0].trim() + "<|>" + parts[1] + "<|>" + numberOfYears));
            }
        });

        /** Scheduling algorithm to find the birthday users **/
        List<String> birthdayList = commonServiceRepository.getBirthdayAndAnniversaryUsersList(LocalDate.now().getMonthValue(), LocalDate.now().getDayOfMonth(), "Anniversary");
        birthdayList.forEach(user -> {
            String[] parts = user.split("-");
            int numberOfYears = LocalDate.now().getYear() - Integer.parseInt(parts[2]);
            if (numberOfYears > 0) {
                applicationEventPublisher.publishEvent(new NotificationQueueDto(NotificationQueueEnum.USER_BIRTHDAY_MAIL.name(), parts[0].trim() + "<|>" + parts[1]));
            }
        });

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
