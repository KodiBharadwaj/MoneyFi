package com.example.apigateway.repository;

import com.example.apigateway.model.BlackListedToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenBlackListRepository extends JpaRepository<BlackListedToken, Integer> {

    public BlackListedToken findByToken(String token);
}
