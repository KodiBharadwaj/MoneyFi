package com.moneyfi.user.repository.general;

import com.moneyfi.user.model.general.EmailNotificationFailedDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface EmailNotificationFailedRepository extends JpaRepository<EmailNotificationFailedDetails, Long> {

    @Query("""
            SELECT e
            FROM EmailNotificationFailedDetails e
            WHERE e.createdAt BETWEEN :startDate AND :endDate
            AND (
                    :search IS NULL
                    OR :search = ''
                    OR LOWER(e.mdcRequestId) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(e.referenceNumber) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(e.username) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(e.message) LIKE LOWER(CONCAT('%', :search, '%'))
                )
            """)
    Page<EmailNotificationFailedDetails> getFailedQueueEmailNotificationDetails(@Param("startDate") LocalDateTime startDate,
                                                                                @Param("endDate") LocalDateTime endDate,
                                                                                @Param("search") String search,
                                                                                Pageable pageable);

}
