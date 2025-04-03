package com.moneyfi.apigateway.repository;

import com.moneyfi.apigateway.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {

    public User findByUsername(String username);

    @Query("select u from User u where u.otpCount > 3")
    public List<User> getUserListWhoseOtpCountGreaterThanThree();
}
