package com.moneyfi.apigateway.repository.gmailsync;

import com.moneyfi.apigateway.model.gmailsync.GmailAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface GmailSyncRepository extends JpaRepository<GmailAuth, Long> {

    Optional<GmailAuth> findByUserId(Long userId);

    @Query("SELECT g FROM GmailAuth g WHERE g.userId = :userId")
    Optional<GmailAuth> existsByUserId(Long userId);

    @Query("SELECT g from GmailAuth g WHERE g.count >= 3")
    List<GmailAuth> getTransactionsListWhoseCountIsGreaterThanThree();

    @Modifying
    @Transactional
    void deleteByUserId(Long userId);
}
