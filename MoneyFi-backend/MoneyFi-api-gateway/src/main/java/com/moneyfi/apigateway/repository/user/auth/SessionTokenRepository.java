package com.moneyfi.apigateway.repository.user.auth;

import com.moneyfi.apigateway.model.auth.SessionTokenModel;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface SessionTokenRepository extends JpaRepository<SessionTokenModel, Long> {

    @Query("SELECT s FROM SessionTokenModel s WHERE s.username = :username")
    SessionTokenModel findByUsername(String username);

    @Query("SELECT s FROM SessionTokenModel s WHERE s.token = :token")
    SessionTokenModel getSessionTokenModelByToken(String token);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM session_token_table WHERE username = :username")
    void deleteAllByUsername(String username);
}
