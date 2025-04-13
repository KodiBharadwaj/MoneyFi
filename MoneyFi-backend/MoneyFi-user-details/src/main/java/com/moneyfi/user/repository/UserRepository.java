package com.moneyfi.user.repository;

import com.moneyfi.user.model.UserDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserDetails, Long> {

    UserDetails findByUserId(Long userId);
}