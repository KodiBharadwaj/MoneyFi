package com.moneyfi.user.repository;

import com.moneyfi.user.model.UserDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<UserDetails, Long> {

    @Query(nativeQuery = true, value = "exec getUserDetailsByUserId @userId = :userId")
    UserDetails findByUserId(Long userId);
}