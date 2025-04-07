package com.moneyfi.apigateway.repository;

import com.moneyfi.apigateway.model.UserAuthModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<UserAuthModel, Long> {

    UserAuthModel findByUsername(String username);

    @Query("select u from UserAuthModel u where u.otpCount > 2")
    List<UserAuthModel> getUserListWhoseOtpCountGreaterThanThree();
}
