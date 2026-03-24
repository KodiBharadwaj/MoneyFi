package com.moneyfi.user.repository.gmailsync;

import com.moneyfi.user.model.gmailsync.GmailSyncHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GmailSyncHistoryRepository extends JpaRepository<GmailSyncHistory, Long> {

    /** Spring JPA */
    List<GmailSyncHistory> findByUserId(Long userId);
}
