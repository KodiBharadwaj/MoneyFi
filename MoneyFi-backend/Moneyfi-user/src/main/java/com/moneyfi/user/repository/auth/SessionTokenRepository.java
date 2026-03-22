package com.moneyfi.user.repository.auth;

import com.moneyfi.user.model.auth.SessionTokenModel;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionTokenRepository extends JpaRepository<SessionTokenModel, Long> {

    /** JPQL */
    @Query("SELECT s FROM SessionTokenModel s WHERE s.username = :username")
    SessionTokenModel findByUsername(String username);

    /** JPQL */
    @Query("SELECT s FROM SessionTokenModel s WHERE s.token = :token")
    SessionTokenModel getSessionTokenModelByToken(String token);

    /** SQL Native Query */
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM session_token_table WHERE username = :username")
    void deleteAllByUsername(String username);
}
