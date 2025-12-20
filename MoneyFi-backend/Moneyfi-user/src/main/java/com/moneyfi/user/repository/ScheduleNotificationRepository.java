package com.moneyfi.user.repository;

import com.moneyfi.user.model.ScheduleNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleNotificationRepository extends JpaRepository<ScheduleNotification, Long> {
}
