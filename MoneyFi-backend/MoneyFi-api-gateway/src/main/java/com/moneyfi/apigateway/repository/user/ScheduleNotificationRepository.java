package com.moneyfi.apigateway.repository.user;

import com.moneyfi.apigateway.model.common.ScheduleNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleNotificationRepository extends JpaRepository<ScheduleNotification, Long> {
}
