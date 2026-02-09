package com.moneyfi.apigateway.repository.gmailsync;

import com.moneyfi.apigateway.model.gmailsync.GmailAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GmailSyncRepository extends JpaRepository<GmailAuth, Long> {

    Optional<GmailAuth> findByUserId(Long userId);

    @Query("SELECT g from GmailAuth g WHERE g.count >= 3")
    List<GmailAuth> getTransactionsListWhoseCountIsGreaterThanThree();
}
