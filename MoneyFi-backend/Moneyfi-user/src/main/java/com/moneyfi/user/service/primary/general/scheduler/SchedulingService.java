package com.moneyfi.user.service.primary.general.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moneyfi.constants.enums.NotificationQueueEnum;
import com.moneyfi.user.repository.primary.common.CommonServiceRepository;
import com.moneyfi.user.repository.primary.gmailsync.GmailSyncRepository;
import com.moneyfi.user.repository.primary.auth.TokenBlackListRepository;
import com.moneyfi.user.repository.primary.auth.UserRepository;
import com.moneyfi.user.service.primary.NotificationDetailsSetter;
import com.moneyfi.user.service.primary.ReusableCodeClass;
import com.moneyfi.user.service.primary.general.inter.EmailNotificationFailedService;
import com.moneyfi.user.service.primary.general.scheduler.dto.UserEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static com.moneyfi.user.service.primary.NotificationDetailsSetter.setUserDetailsForNotificationQueueAdminServiceForSendingAnniversary;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulingService {

    private final TokenBlackListRepository tokenBlacklistRepository;
    private final UserRepository userRepository;
    private final CommonServiceRepository commonServiceRepository;
    private final GmailSyncRepository gmailSyncRepository;
    private final ReusableCodeClass reusableCodeClass;
    private final ObjectMapper objectMapper;
    private final EmailNotificationFailedService emailNotificationFailedService;

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

    private void functionCallToSendBirthdayEmailToUsers() {
        List<UserEventDto> birthdayList = commonServiceRepository.getBirthdayAndAnniversaryUsersList(LocalDate.now().getMonthValue(), LocalDate.now().getDayOfMonth(), "Anniversary");
        birthdayList.forEach(user -> {
            String email = user.getUsername();
            String name = user.getName();
            int year = user.getYear();
            int numberOfYears = LocalDate.now(ZoneId.systemDefault()).getYear() - year;

            if (numberOfYears > 0) {
                try {
                    reusableCodeClass.sendNotificationToUser(
                            NotificationQueueEnum.USER_BIRTHDAY_MAIL.name(),
                            objectMapper.writeValueAsString(NotificationDetailsSetter.setUserDetailsForNotificationQueue(email, name, NotificationQueueEnum.USER_BIRTHDAY_MAIL.name(), emailNotificationFailedService))
                    );
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void functionCallToSendAnniversaryEmailToUsers() {
        List<UserEventDto> anniversaryUsersList = commonServiceRepository.getBirthdayAndAnniversaryUsersList(LocalDate.now().getMonthValue(), LocalDate.now().getDayOfMonth(), "Birthday");
        anniversaryUsersList.forEach(user -> {
            String email = user.getUsername();
            String name = user.getName();
            int year = user.getYear();
            int numberOfYears = LocalDate.now(ZoneId.systemDefault()).getYear() - year;

            if (numberOfYears != 0) {
                try {
                    reusableCodeClass.sendNotificationToUser(
                            NotificationQueueEnum.USER_ANNIVERSARY_MAIL.name(),
                            objectMapper.writeValueAsString(setUserDetailsForNotificationQueueAdminServiceForSendingAnniversary(email, name, numberOfYears, NotificationQueueEnum.USER_ANNIVERSARY_MAIL.name(), emailNotificationFailedService))
                    );
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
