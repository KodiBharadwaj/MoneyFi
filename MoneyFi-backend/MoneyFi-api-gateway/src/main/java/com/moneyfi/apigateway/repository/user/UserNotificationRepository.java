package com.moneyfi.apigateway.repository.user;

import com.moneyfi.apigateway.model.common.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {

    @Query("select u from  UserNotification u where u.id = :notificationId and u.username = :username")
    UserNotification findByScheduleIdAndUsername(Long notificationId, String username);
}
