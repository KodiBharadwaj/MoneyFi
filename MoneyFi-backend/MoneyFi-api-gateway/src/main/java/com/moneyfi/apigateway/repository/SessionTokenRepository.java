package com.moneyfi.apigateway.repository;

import com.moneyfi.apigateway.model.SessionTokenModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionTokenRepository extends JpaRepository<SessionTokenModel, Long> {

    SessionTokenModel findByUsername(String username);

    SessionTokenModel findByToken(String token);
}
