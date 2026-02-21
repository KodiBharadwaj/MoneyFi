package com.moneyfi.apigateway.model.auth;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "blacklist_token_table")
public class BlackListedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String token;

    @Column(nullable = false)
    private Date expiry;

    @Column(nullable = false)
    private String username;

    public BlackListedToken(String token, Date expiry, String username) {
        this.token = token;
        this.expiry = expiry;
        this.username = username;
    }
}
