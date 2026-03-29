package com.moneyfi.user.model.auth;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@Table(name = "user_auth_table")
@Entity
public class UserAuthModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String username;
    private String password;
    private String verificationCode;
    private LocalDateTime verificationCodeExpiration;
    private int otpCount;
    private boolean isBlocked;
    private boolean isDeleted;
    @Column(nullable = false)
    private Integer loginCodeValue;
    private int roleId;

    @Column(nullable = false)
    private LocalDateTime lastReset;

    @PrePersist
    public void function() {
        this.lastReset = LocalDateTime.now();
    }
}
