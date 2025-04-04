package com.moneyfi.apigateway.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "blacklisted_tokens", uniqueConstraints = @UniqueConstraint(columnNames = "token"))
public class BlackListedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true) // Ensure unique tokens
    private String token;

    @Column(nullable = false)
    private Date expiry;

    public BlackListedToken(String token, Date expiry) {
    }
}
