package com.example.apigateway.repository;

import com.example.apigateway.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<User, Integer> {

    public User findByUsername(String username);
}
