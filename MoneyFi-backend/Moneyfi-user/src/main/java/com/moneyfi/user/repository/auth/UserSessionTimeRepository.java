package com.moneyfi.user.repository.auth;

import com.moneyfi.user.model.auth.UserSessionTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSessionTimeRepository extends JpaRepository<UserSessionTime, Long> {

    /** Spring JPA */
    Optional<UserSessionTime> findByUserId(Long id);
}
