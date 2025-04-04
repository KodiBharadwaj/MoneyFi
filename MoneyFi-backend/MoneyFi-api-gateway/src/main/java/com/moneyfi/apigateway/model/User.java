package com.moneyfi.apigateway.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@Table(name = "users")
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;

    private String verificationCode;
    private LocalDateTime verificationCodeExpiration;
    private int otpCount;

}
