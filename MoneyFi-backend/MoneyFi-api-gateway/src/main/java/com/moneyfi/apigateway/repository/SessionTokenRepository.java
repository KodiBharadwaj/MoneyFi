package com.moneyfi.apigateway.repository;

import com.moneyfi.apigateway.model.SessionTokenModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionTokenRepository extends JpaRepository<SessionTokenModel, Long> {

    public SessionTokenModel findByUsername(String username);

    public SessionTokenModel findByToken(String token);

}
