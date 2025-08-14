package com.moneyfi.apigateway.model.common;

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
    private boolean isCancelled;
    private boolean isActive;
}
