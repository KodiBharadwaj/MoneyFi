package com.moneyfi.apigateway.repository.user.auth;

import com.moneyfi.apigateway.model.auth.UserAuthModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface UserRepository extends JpaRepository<UserAuthModel, Long> {

    @Query(nativeQuery = true, value = "exec getUserAuthDetailsListWhoseOtpCountGreaterThanThree @startOfToday = :startOfToday")
    List<UserAuthModel> getUserListWhoseOtpCountGreaterThanThree(LocalDateTime startOfToday);

    @Query(nativeQuery = true, value = "exec getUserAuthDetailsByUsername @username = :email")
    UserAuthModel getUserDetailsByUsername(String email);
}
