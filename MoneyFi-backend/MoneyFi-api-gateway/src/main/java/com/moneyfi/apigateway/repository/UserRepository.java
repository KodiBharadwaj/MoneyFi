package com.moneyfi.apigateway.repository;

import com.moneyfi.apigateway.model.UserAuthModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<UserAuthModel, Long> {

    @Query(nativeQuery = true, value = "exec findByUsernameFromUserAuthTable @username = :username")
    UserAuthModel findByUsername(String username);

    @Query(nativeQuery = true, value = "exec getUserListWhoseOtpCountGreaterThanThree")
    List<UserAuthModel> getUserListWhoseOtpCountGreaterThanThree();
}
