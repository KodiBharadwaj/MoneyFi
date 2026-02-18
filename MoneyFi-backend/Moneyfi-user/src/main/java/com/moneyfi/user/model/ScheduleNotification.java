package com.moneyfi.user.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    private Long scheduleBy;
    private Long updatedBy;
    private Long parentKey;
    private String notificationType;

    @PrePersist
    public void init() {
        LocalDateTime currentTime = LocalDateTime.now();
        this.createdDate = currentTime;
        this.updatedAt = currentTime;
        this.setActive(true);
        this.setCancelled(false);
    }
}
