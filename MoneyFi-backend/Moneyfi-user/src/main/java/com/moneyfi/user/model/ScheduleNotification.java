package com.moneyfi.user.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static com.moneyfi.user.util.constants.StringConstants.CURRENT_DATE_TIME;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "schedule_notification_table")
public class ScheduleNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String subject;
    private String description;
    private LocalDateTime scheduleFrom;
    private LocalDateTime scheduleTo;
    private String recipients;
    private LocalDateTime createdDate;
    private LocalDateTime updatedAt;
    private boolean isCancelled;
    private boolean isActive;

    @PrePersist
    public void init() {
        this.createdDate = CURRENT_DATE_TIME;
        this.updatedAt = CURRENT_DATE_TIME;
        this.setActive(true);
        this.setCancelled(false);
    }
}
