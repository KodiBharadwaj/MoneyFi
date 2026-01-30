package com.moneyfi.apigateway.repository.gmailsync;

import com.moneyfi.apigateway.model.gmailsync.GmailSyncHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GmailSyncHistoryRepository extends JpaRepository<GmailSyncHistory, Long> {
    List<GmailSyncHistory> findByUserId(Long userId);
}
