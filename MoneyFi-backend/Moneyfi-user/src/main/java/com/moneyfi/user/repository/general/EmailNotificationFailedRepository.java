package com.moneyfi.user.repository.general;

import com.moneyfi.user.model.general.EmailNotificationFailedDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailNotificationFailedRepository extends JpaRepository<EmailNotificationFailedDetails, Long> {
}
