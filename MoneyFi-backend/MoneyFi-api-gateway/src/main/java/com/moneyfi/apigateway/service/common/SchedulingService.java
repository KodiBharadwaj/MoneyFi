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

    public SchedulingService(TokenBlackListRepository tokenBlacklistRepository,
                             UserRepository userRepository,
                             CommonServiceRepository commonServiceRepository){
        this.tokenBlacklistRepository = tokenBlacklistRepository;
        this.userRepository = userRepository;
        this.commonServiceRepository = commonServiceRepository;
    }

    @PostConstruct
    public void initializeScheduledMethodsInCaseOfServiceRunningDelay(){
        dailyJobRunInBeginningOfTheDay();
        runOnFirstDayOfMonthAtMidnightForRecurringIncomeAndExpense();
    }

    @Scheduled(fixedRate = 3600000) // Runs every 1 hour
    @Transactional
    public void removeExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Checking for expired tokens at: " + now);
        tokenBlacklistRepository.deleteByExpiryBefore(now);  // Deletes expired tokens
    }

    @Scheduled(fixedRate = 3600000) // Method Runs for every 1 hour
    public void removeOtpCountOfPreviousDay(){
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        List<UserAuthModel> userAuthModelList = userRepository.getUserListWhoseOtpCountGreaterThanThree(startOfToday);
        for (UserAuthModel userAuthModel : userAuthModelList) {
            userAuthModel.setOtpCount(0);
            userRepository.save(userAuthModel);
        }
    }

    @Scheduled(cron = "0 0 0 * * *") // Runs at every 12 am of the day (starting of the day)
    public void dailyJobRunInBeginningOfTheDay(){
        List<String> birthdayList = commonServiceRepository.getBirthdayUserNames(LocalDate.now().getMonthValue(), LocalDate.now().getDayOfMonth());
        new Thread(() -> birthdayList.forEach(user -> {
            String[] parts = user.split("-");
            EmailTemplates.sendBirthdayMail(parts[0].trim(), parts[1], LocalDate.now().getYear()-Integer.parseInt(parts[2]));
        })).start();
    }

    @Scheduled(cron = "0 0 0 1 * *") //Runs on 1st of every month
    public void runOnFirstDayOfMonthAtMidnightForRecurringIncomeAndExpense() {
        userRepository.updateRecurringIncomesAndExpenses();
    }

}
