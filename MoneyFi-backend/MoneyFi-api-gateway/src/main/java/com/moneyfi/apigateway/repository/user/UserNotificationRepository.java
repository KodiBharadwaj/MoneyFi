package com.moneyfi.apigateway.repository.user;

import com.moneyfi.apigateway.model.common.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {

    @Query("select u from  UserNotification u where u.id = :notificationId and u.username = :username")
    UserNotification findByScheduleIdAndUsername(Long notificationId, String username);

    @Query("select u from UserNotification u where u.scheduleId = :scheduleId and u.isRead = true")
    List<UserNotification> findByScheduleId(Long scheduleId);
}
