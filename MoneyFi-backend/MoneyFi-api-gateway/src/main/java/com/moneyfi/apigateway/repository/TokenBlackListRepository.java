package com.moneyfi.apigateway.repository;

import com.moneyfi.apigateway.model.BlackListedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface TokenBlackListRepository extends JpaRepository<BlackListedToken, Long> {

    public BlackListedToken findByToken(String token);

    void deleteByExpiryBefore(LocalDateTime now);
}
