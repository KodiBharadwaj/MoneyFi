package com.moneyfi.apigateway.repository.auth;

import com.moneyfi.apigateway.model.auth.SessionTokenModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SessionTokenRepository extends JpaRepository<SessionTokenModel, Long> {

    @Query(nativeQuery = true, value = "exec getSessionTokenModelByUsername  @username = :username")
    SessionTokenModel findByUsername(String username);

    @Query(nativeQuery = true, value = "exec getSessionTokenModelByToken @token = :token")
    SessionTokenModel getSessionTokenModelByToken(String token);
}
