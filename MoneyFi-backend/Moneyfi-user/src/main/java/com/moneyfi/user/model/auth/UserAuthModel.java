package com.moneyfi.user.model.auth;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@Table(name = "user_auth_table")
@Entity
public class UserAuthModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    @Column(unique = true)
    private String username;
    private String password;
    private String verificationCode;
    private LocalDateTime verificationCodeExpiration;
    private int otpCount;
    private boolean isBlocked;
    private boolean isDeleted;
    @NotNull
    private Integer loginCodeValue;
    @NotNull
    private int roleId;

    @NotNull
    private LocalDateTime lastReset;

    @PrePersist
    public void function() {
        this.lastReset = LocalDateTime.now();
    }
}
