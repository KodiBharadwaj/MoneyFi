package com.moneyfi.user.repository;

import com.moneyfi.user.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserModel, Long> {

    public UserModel findByUserId(Long userId);
}