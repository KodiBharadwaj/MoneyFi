package com.moneyfi.user.repository.primary.general;

import com.moneyfi.user.model.primary.general.ScheduleNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleNotificationRepository extends JpaRepository<ScheduleNotification, Long> {

    /** Spring JPA */
    List<ScheduleNotification> findByScheduleBy(Long id);
}
