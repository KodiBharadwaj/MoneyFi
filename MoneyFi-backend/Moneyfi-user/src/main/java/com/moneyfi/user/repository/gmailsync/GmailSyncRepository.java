package com.moneyfi.user.repository.gmailsync;

import com.moneyfi.user.model.gmailsync.GmailAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GmailSyncRepository extends JpaRepository<GmailAuth, Long> {

    /** Spring JPA */
    Optional<GmailAuth> findByUserId(Long userId);

    /** JPQL */
    @Query("SELECT g from GmailAuth g WHERE g.count >= 3")
    List<GmailAuth> getTransactionsListWhoseCountIsGreaterThanThree();
}
