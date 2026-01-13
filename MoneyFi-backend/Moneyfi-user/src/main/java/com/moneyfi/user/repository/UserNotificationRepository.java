package com.moneyfi.user.repository;

import com.moneyfi.user.model.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {

    @Query("select u from UserNotification u where u.id = :notificationId and u.username = :username")
    Optional<UserNotification> findByScheduleIdAndUsername(Long notificationId, String username);

    @Query("select u from UserNotification u where u.scheduleId = :scheduleId and u.isRead = true")
    List<UserNotification> findByScheduleId(Long scheduleId);

    @Modifying
    @Transactional
    void deleteAllByScheduleId(Long scheduleId);
}
