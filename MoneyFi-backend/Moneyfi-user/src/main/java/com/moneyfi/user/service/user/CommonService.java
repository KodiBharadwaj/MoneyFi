package com.moneyfi.user.service.user;

import com.moneyfi.user.model.general.UserNotification;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface CommonService {
    boolean sendAccountStatementEmail(String username, Long userId, byte[] pdfBytes);

    boolean sendSpendingAnalysisEmail(String username, Long userId, byte[] pdfBytes);

    SseEmitter addEmitterForNotification(String username);

    void asyncNotificationHandler(List<UserNotification> userNotificationListForSpecifiedUsers, Long scheduleId);

    boolean isTokenBlacklisted(String token, String username);
}
