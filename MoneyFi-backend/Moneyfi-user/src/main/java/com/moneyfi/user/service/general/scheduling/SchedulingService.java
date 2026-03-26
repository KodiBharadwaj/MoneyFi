package com.moneyfi.user.service.general.scheduling;

import com.moneyfi.constants.enums.NotificationQueueEnum;
import com.moneyfi.user.repository.common.CommonServiceRepository;
import com.moneyfi.user.repository.gmailsync.GmailSyncRepository;
import com.moneyfi.user.repository.auth.TokenBlackListRepository;
import com.moneyfi.user.repository.auth.UserRepository;
import com.moneyfi.user.service.general.scheduling.dto.UserEventDto;
import com.moneyfi.user.service.user.dto.internal.NotificationQueueDto;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SchedulingService {

    private final TokenBlackListRepository tokenBlacklistRepository;
    private final UserRepository userRepository;
    private final CommonServiceRepository commonServiceRepository;
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
        userRepository.updateOtpCountToResetForUsers();
        userRepository.deleteAllUserDataForDeletedUsersAfter30Days();
        gmailSyncRepository.updateGmailCountToResetForUsers();

        functionCallToSendAnniversaryEmailToUsers();
        functionCallToSendBirthdayEmailToUsers();
    }

    @Scheduled(cron = "0 0 0 1 * *") //Runs on 1st of every month
    public void runOnFirstDayOfMonthAtMidnightForRecurringIncomeAndExpense() {
        userRepository.updateRecurringIncomesAndExpenses();
    }

    private void functionCallToSendBirthdayEmailToUsers() {
        List<UserEventDto> birthdayList = commonServiceRepository.getBirthdayAndAnniversaryUsersList(LocalDate.now().getMonthValue(), LocalDate.now().getDayOfMonth(), "Anniversary");
        birthdayList.forEach(user -> {
            String email = user.getUsername();
            String name = user.getName();
            int year = user.getYear();
            int numberOfYears = LocalDate.now().getYear() - year;

            if (numberOfYears > 0) {
                applicationEventPublisher.publishEvent(new NotificationQueueDto(NotificationQueueEnum.USER_BIRTHDAY_MAIL.name(), email + "<|>" + name));
            }
        });
    }

    private void functionCallToSendAnniversaryEmailToUsers() {
        List<UserEventDto> anniversaryUsersList = commonServiceRepository.getBirthdayAndAnniversaryUsersList(LocalDate.now().getMonthValue(), LocalDate.now().getDayOfMonth(), "Birthday");
        anniversaryUsersList.forEach(user -> {
            String email = user.getUsername();
            String name = user.getName();
            int year = user.getYear();
            int numberOfYears = LocalDate.now().getYear() - year;

            if (numberOfYears != 0) {
                applicationEventPublisher.publishEvent(new NotificationQueueDto(NotificationQueueEnum.USER_ANNIVERSARY_MAIL.name(), email + "<|>" + name + "<|>" + numberOfYears));
            }
        });
    }
}
