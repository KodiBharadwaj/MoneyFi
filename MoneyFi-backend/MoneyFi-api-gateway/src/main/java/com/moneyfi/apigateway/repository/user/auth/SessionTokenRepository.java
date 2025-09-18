package com.moneyfi.apigateway.repository.user.auth;

import com.moneyfi.apigateway.model.auth.SessionTokenModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SessionTokenRepository extends JpaRepository<SessionTokenModel, Long> {

    @Query("SELECT s FROM SessionTokenModel s WHERE s.username = :username")
    SessionTokenModel findByUsername(String username);

    @Query("SELECT s FROM SessionTokenModel s WHERE s.token = :token")
    SessionTokenModel getSessionTokenModelByToken(String token);
}
