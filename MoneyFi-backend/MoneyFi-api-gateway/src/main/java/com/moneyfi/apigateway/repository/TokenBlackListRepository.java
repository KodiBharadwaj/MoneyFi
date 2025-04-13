package com.moneyfi.apigateway.repository;

import com.moneyfi.apigateway.model.BlackListedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface TokenBlackListRepository extends JpaRepository<BlackListedToken, Long> {

    @Query(nativeQuery = true, value = "exec findBlackListByToken @token = :token")
    List<BlackListedToken> findByToken(String token);

    void deleteByExpiryBefore(LocalDateTime now);
}
