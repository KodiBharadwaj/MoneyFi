package com.moneyfi.user.repository.gmailsync;

import com.moneyfi.user.model.gmailsync.GmailSyncHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GmailSyncHistoryRepository extends JpaRepository<GmailSyncHistory, Long> {

    /** Spring JPA */
    List<GmailSyncHistory> findByUserId(Long userId);

    @Query("SELECT g FROM GmailSyncHistory g WHERE g.userId = :userId AND MONTH(g.syncTime) = :month")
    List<GmailSyncHistory> findByUserIdAndByCurrentMonth(@Param("userId") Long userId, @Param("month") int monthValue);
}
