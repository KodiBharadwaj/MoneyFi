package com.moneyfi.apigateway.repository.user.auth;

import com.moneyfi.apigateway.model.auth.BlackListedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface TokenBlackListRepository extends JpaRepository<BlackListedToken, Long> {

    @Query("SELECT b FROM BlackListedToken b WHERE b.username = :username AND b.token = :token")
    List<BlackListedToken> findByUsernameAndToken(String username, String token);

    void deleteByExpiryBefore(LocalDateTime now);
}
