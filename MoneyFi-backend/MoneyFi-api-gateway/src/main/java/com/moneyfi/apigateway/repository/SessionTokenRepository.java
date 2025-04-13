package com.moneyfi.apigateway.repository;

import com.moneyfi.apigateway.model.SessionTokenModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SessionTokenRepository extends JpaRepository<SessionTokenModel, Long> {

    @Query(nativeQuery = true, value = "exec findByUsername  @username = :username")
    SessionTokenModel findByUsername(String username);

    @Query(nativeQuery = true, value = "exec findByToken @token = :token")
    SessionTokenModel findByToken(String token);
}
