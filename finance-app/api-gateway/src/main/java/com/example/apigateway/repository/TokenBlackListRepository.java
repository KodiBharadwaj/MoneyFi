package com.example.apigateway.repository;

import com.example.apigateway.model.BlackListedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface TokenBlackListRepository extends JpaRepository<BlackListedToken, Integer> {

    public BlackListedToken findByToken(String token);

    void deleteByExpiryBefore(LocalDateTime now);
}
