package com.moneyfi.apigateway.repository.user.auth;

import com.moneyfi.apigateway.model.auth.UserAuthModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface UserRepository extends JpaRepository<UserAuthModel, Long> {

    @Query(nativeQuery = true, value = "exec getUserAuthDetailsByUsername @username = :username")
    UserAuthModel findByUsername(String username);

    @Query(nativeQuery = true, value = "exec getUserAuthDetailsListWhoseOtpCountGreaterThanThree @startOfToday = :startOfToday")
    List<UserAuthModel> getUserListWhoseOtpCountGreaterThanThree(LocalDateTime startOfToday);

    @Query("select u from UserAuthModel u where u.username = :email")
    UserAuthModel getBlockedUsers(String email);
}
