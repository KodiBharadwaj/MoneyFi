package com.moneyfi.user.repository.auth;

import com.moneyfi.user.model.auth.BlackListedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TokenBlackListRepository extends JpaRepository<BlackListedToken, Long> {

    /** JPQL */
    @Query("SELECT b FROM BlackListedToken b WHERE b.username = :username AND b.token = :token")
    List<BlackListedToken> findByUsernameAndToken(String username, String token);

    /** Spring JPA */
    void deleteByExpiryBefore(LocalDateTime now);
}
