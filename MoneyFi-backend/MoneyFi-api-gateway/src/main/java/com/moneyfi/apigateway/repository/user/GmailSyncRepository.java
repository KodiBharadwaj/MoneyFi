package com.moneyfi.apigateway.repository.user;

import com.moneyfi.apigateway.model.common.GmailAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GmailSyncRepository extends JpaRepository<GmailAuth, Long> {

    Optional<GmailAuth> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    void deleteByUserId(Long userId);
}
