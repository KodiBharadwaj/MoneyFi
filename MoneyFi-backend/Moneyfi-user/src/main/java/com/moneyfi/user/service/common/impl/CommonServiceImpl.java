package com.moneyfi.user.service.common.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.moneyfi.user.model.UserNotification;
import com.moneyfi.user.repository.ProfileRepository;
import com.moneyfi.user.repository.common.CommonServiceRepository;
import com.moneyfi.user.service.common.CommonService;
import com.moneyfi.user.service.common.dto.internal.GmailSyncCountJsonDto;
import com.moneyfi.user.service.common.dto.response.UserNotificationResponseDto;
import com.moneyfi.user.util.EmailTemplates;
import com.moneyfi.user.util.constants.StringConstants;
import com.moneyfi.user.util.enums.RaiseRequestStatus;
import com.moneyfi.user.util.enums.RequestReason;
import com.moneyfi.user.util.enums.UserRequestType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.moneyfi.user.util.constants.StringConstants.functionToGetNameOfUserWithUserId;

@Service
@Slf4j
public class CommonServiceImpl implements CommonService {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    private final ProfileRepository profileRepository;
    private final CommonServiceRepository commonServiceRepository;
    private final EmailTemplates emailTemplates;

    public CommonServiceImpl(ProfileRepository profileRepository,
                             CommonServiceRepository commonServiceRepository,
                             EmailTemplates emailTemplates) {
        this.profileRepository = profileRepository;
        this.commonServiceRepository = commonServiceRepository;
        this.emailTemplates = emailTemplates;
    }

    @Override
    public boolean sendAccountStatementEmail(String username, Long userId, byte[] pdfBytes) {
        String name = functionToGetNameOfUserWithUserId(profileRepository, userId);
        try {
            return emailTemplates.sendAccountStatementAsEmail(!name.trim().isEmpty() ? name : "User", username, pdfBytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean sendSpendingAnalysisEmail(String username, Long userId, byte[] pdfBytes) {
        String name = functionToGetNameOfUserWithUserId(profileRepository, userId);
        try {
            return emailTemplates.sendSpendingAnalysisEmail(!name.trim().isEmpty() ? name : "User", username, pdfBytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SseEmitter addEmitterForNotification(String username) {
        log.info("checking username: {}", username);
        SseEmitter emitter = new SseEmitter(0L);
        emitters.put(username, emitter);
        log.info("checking emitter: {}", emitters);
        emitter.onCompletion(() -> emitters.remove(username));
        emitter.onTimeout(() -> emitters.remove(username));
        emitter.onError(e -> emitters.remove(username));
        return emitter;
    }

    @Async
    public void asyncNotificationHandler(List<UserNotification> notificationsList, Long scheduleId) {
        notificationsList.forEach(notification -> {
            sendLatestNotificationSeamlessly(notification.getUsername(), scheduleId);
            sendNotificationCountSeamlessly(notification.getUsername());
        });
    }

    private void sendLatestNotificationSeamlessly(String username, Long scheduleId) {
        Optional<UserNotificationResponseDto> latestNotification = commonServiceRepository.getUserNotifications(username, "ACTIVE")
                .stream()
                .filter(notification -> notification.getScheduleId().equals(scheduleId) && !notification.isRead())
                .findFirst();
        log.info("checking username: {}", username);
        log.info("checking latest record: {}", latestNotification);
        latestNotification.ifPresent(userNotificationResponseDto -> {
            if(userNotificationResponseDto.getNotificationType().equalsIgnoreCase(UserRequestType.GMAIL_SYNC_COUNT_INCREASE.name())) {
                try {
                    userNotificationResponseDto.setDescription(StringConstants.objectMapper.readValue(userNotificationResponseDto.getDescription(), GmailSyncCountJsonDto.class).getReason());
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
            pushLatestNotification(username, userNotificationResponseDto);
        });
    }

    private void sendNotificationCountSeamlessly(String username) {
        pushNotificationCount(username, Math.toIntExact(commonServiceRepository.getUserNotifications(username, "ACTIVE")
                .stream()
                .filter(notification -> !notification.isRead())
                .count()));
    }

    private void pushLatestNotification(String username, UserNotificationResponseDto userNotification) {
        pushEvent(username, "notification", userNotification);
    }

    private void pushNotificationCount(String username, Integer notificationCount) {
        pushEvent(username, "notification-count", notificationCount);
    }

    private void pushEvent(String username, String eventName, Object data) {
        SseEmitter emitter = emitters.get(username);
        log.info("SSE push [{}] for user {} -> emitter: {}", eventName, username, emitter);
        if (emitter != null) {
            try {
                emitter.send(
                        SseEmitter.event()
                                .name(eventName)
                                .data(data)
                );
            } catch (Exception e) {
                log.error("SSE error for user {}. Removing emitter.", username, e);
                emitters.remove(username);
            }
        }
    }
}
