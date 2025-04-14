package com.moneyfi.apigateway.repository;

import com.moneyfi.apigateway.model.UserAuthModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<UserAuthModel, Long> {

    @Query(nativeQuery = true, value = "exec getUserAuthDetailsByUsername @username = :username")
    UserAuthModel findByUsername(String username);

    @Query(nativeQuery = true, value = "exec getUserAuthDetailsListWhoseOtpCountGreaterThanThree")
    List<UserAuthModel> getUserListWhoseOtpCountGreaterThanThree();
}
