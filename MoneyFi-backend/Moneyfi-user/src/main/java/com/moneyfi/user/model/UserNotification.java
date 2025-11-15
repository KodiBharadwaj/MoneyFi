package com.moneyfi.user.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_notification_table")
public class UserNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private Long scheduleId;
    private boolean isRead;

    public UserNotification(String username, Long scheduleId, boolean isRead) {
        this.username = username;
        this.scheduleId = scheduleId;
        this.isRead = isRead;
    }
}
