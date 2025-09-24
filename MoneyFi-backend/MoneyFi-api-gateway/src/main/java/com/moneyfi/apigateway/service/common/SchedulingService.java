package com.moneyfi.apigateway.service.common;

import com.moneyfi.apigateway.model.auth.UserAuthModel;
import com.moneyfi.apigateway.repository.common.CommonServiceRepository;
import com.moneyfi.apigateway.repository.user.auth.TokenBlackListRepository;
import com.moneyfi.apigateway.repository.user.auth.UserRepository;
import com.moneyfi.apigateway.util.EmailTemplates;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class SchedulingService {

    private final TokenBlackListRepository tokenBlacklistRepository;
    private final UserRepository userRepository;
    private final CommonServiceRepository commonServiceRepository;
    private final EmailTemplates emailTemplates;

    public SchedulingService(TokenBlackListRepository tokenBlacklistRepository,
                             UserRepository userRepository,
                             CommonServiceRepository commonServiceRepository,
                             EmailTemplates emailTemplates){
        this.tokenBlacklistRepository = tokenBlacklistRepository;
        this.userRepository = userRepository;
        this.commonServiceRepository = commonServiceRepository;
        this.emailTemplates = emailTemplates;
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
        tokenBlacklistRepository.deleteByExpiryBefore(LocalDateTime.now());
    }

    @Scheduled(cron = "0 0 0 * * *") // Runs at every 12 am of the day (starting of the day)
    public void dailyJobRunInBeginningOfTheDay(){
        /** Scheduling algorithm to remove the previous day otp count **/
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        List<UserAuthModel> userAuthModelList = userRepository.getUserListWhoseOtpCountGreaterThanThree(startOfToday);
        for (UserAuthModel userAuthModel : userAuthModelList) {
            userAuthModel.setOtpCount(0);
            userRepository.save(userAuthModel);
        }

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
            emailTemplates.sendBirthdayWishEmailToUsers(parts[0].trim(), parts[1]);
        })).start();
    }

    @Scheduled(cron = "0 0 0 1 * *") //Runs on 1st of every month
    public void runOnFirstDayOfMonthAtMidnightForRecurringIncomeAndExpense() {
        userRepository.updateRecurringIncomesAndExpenses();
    }

}
